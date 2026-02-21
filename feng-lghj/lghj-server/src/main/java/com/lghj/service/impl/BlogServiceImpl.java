package com.lghj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lghj.constant.RedisConstant;
import com.lghj.context.BaseContext;
import com.lghj.enums.ErrorEnum;
import com.lghj.exception.BusinessException;
import com.lghj.mapper.BlogMapper;
import com.lghj.pojo.dto.BlogUpdateDTO;
import com.lghj.pojo.dto.PageResult;
import com.lghj.pojo.entity.Blog;
import com.lghj.pojo.entity.Follow;
import com.lghj.pojo.entity.User;
import com.lghj.service.IBlogService;
import com.lghj.service.IFollowService;
import com.lghj.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    private final IFollowService followService;
    private final StringRedisTemplate stringRedisTemplate;
    private final IUserService userService;

    /**
     * 发布博客
     */
    @Override
    public void saveBlog(Blog blog) {
        // 获取登录用户
        Long userId = BaseContext.getCurrentId();
        blog.setUserId(userId);
        // 保存探店博文到数据库
        boolean isSuccess = save(blog);
        if (!isSuccess) {
            throw new BusinessException(ErrorEnum.BLOG_SAVE_FAIL);
        }
        // 查询笔记作者的所有粉丝
        List<Follow> follows = followService.query().eq("follow_user_id", userId).list();
        // 推送笔记id给所有粉丝
        for (Follow follow : follows) {
            // 获取粉丝id
            Long followUserId = follow.getUserId();
            // 推送
            String key = RedisConstant.FEED_KEY + followUserId;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }
    }

    /**
     * 点赞功能
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：保证数据库和Redis操作一致性
    public void likeBlog(Long id) {
        // 1. 获取登录用户
        Long userId = BaseContext.getCurrentId();
        // 2. 判断当前登录用户是否已经点赞
        String key = RedisConstant.BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString()); // 获取key对应集合中指定元素的score值
        if (score == null) {
            // 3. 如果未点赞，可以点赞
            // 3.1 数据库点赞数+1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            // 3.2 保存用户到Redis的set集合 zadd key value score
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // 4. 如果已点赞，取消点赞
            // 4.1 数据库点赞数-1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            // 4.2 把用户从Redis的zset集合移除
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
    }

    /**
     * 判断当前登录用户是否点赞
     */
    private void isBlogLiked(Blog blog) {
        // 1. 获取登录用户
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            // 用户未登录，无需查询是否点赞
            return;
        }
        // 2. 判断当前登录用户是否已经点赞
        String key = "blog:liked:" + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null); // 【个人觉得这个其实没必要】
    }

    /**
     * 查询发布博客的用户
     */
    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    /**
     * 根据点赞数量（热度）展示博客
     */
    @Override
    public List<Blog> queryHotBlog(Integer current, Integer size) {
        // 根据用户查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, size));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });
        return records;
    }

    /**
     * 根据id查询博客
     */
    @Override
    public Blog queryBlogById(Long id) {
        // 1. 查询blog
        Blog blog = getById(id);
        if (blog == null) {
            throw new BusinessException(ErrorEnum.BLOG_NOT_EXIST);
        }

        // 2. 查询blog有关的用户
        queryBlogUser(blog);
        // 3. 查询blog是否被点赞
        isBlogLiked(blog);
        return blog;
    }


    /**
     * 粉丝查看关注所有用户博客接口
     */
    @Override
    public PageResult<Blog> queryBlogOfFollow(Integer pageNum, Integer pageSize) {
        // 1. 参数校验与默认值赋值（PC 端分页参数兜底）
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 50) { // 限制最大每页条数，防止查询过多数据
            pageSize = 10;
        }

        // 2. 获取当前登录用户 ID
        Long userId = BaseContext.getCurrentId();
        String key = RedisConstant.FEED_KEY + userId;

        // 3. 核心：Redis ZSet 按「索引区间」查询（适配传统分页，时间倒序）
        // 3.1 计算分页索引：ZSet 索引从 0 开始，降序排列（最新的在最前面，索引 0）
        long startIndex = (long) (pageNum - 1) * pageSize;
        long endIndex = (long) pageNum * pageSize - 1;

        // 3.2 查询指定索引区间的博客 ID（reverseRange：按分值降序查询，返回成员列表）
        Set<String> blogIdStrSet = stringRedisTemplate.opsForZSet()
                .reverseRange(key, startIndex, endIndex); // 逆序获取对应下标的元素（因为要获取最新发布的博客）

        if (blogIdStrSet == null) {
            throw new BusinessException(ErrorEnum.NO_FOLLOW_OTHERS, "暂未关注任何用户");
        }

        // 3.3 查询 Redis ZSet 总记录数（用于计算总页数，PC 端分页必备）
        Long total = stringRedisTemplate.opsForZSet().zCard(key);

        // 4. 非空判断（无数据直接返回空分页结果）
        if (blogIdStrSet == null || blogIdStrSet.isEmpty()) {
            PageResult<Blog> emptyPage = new PageResult<>(0L, 0, pageNum, pageSize, List.of());
            return emptyPage;
        }

        // 5. 解析 Redis 结果，转换为博客 ID 列表
        List<Long> blogIds = blogIdStrSet.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // 6. 批量查询数据库获取博客详情（保证数据顺序与 Redis 一致）
        String idStr = StrUtil.join(",", blogIds);
        List<Blog> blogs = query().in("id", blogIds)
                .last("ORDER BY FIELD(id, " + idStr + ")")
                .list();

        // 7. 补充博客关联信息
        for (Blog blog : blogs) {
            isBlogLiked(blog);
        }

        // 8. 封装 PC 端分页结果（计算总页数）
        Integer totalPage = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        PageResult<Blog> pageResult = new PageResult<>(
                total,
                totalPage,
                pageNum,
                pageSize,
                blogs
        );

        // 9. 返回分页结果
        return pageResult;
    }

    /**
     * 用户删除自己的博客：逻辑删除 + 同步清理Redis所有相关缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：保证数据库和Redis操作一致性
    public void deleteMyBlog(Long blogId) {
        // 1. 获取当前登录用户ID，做登录校验
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException(ErrorEnum.NO_LOGIN, "请先登录再操作");
        }

        // 2. 根据博客ID查询博客，做非空校验
        Blog blog = getById(blogId);
        if (blog == null) {
            throw new BusinessException(ErrorEnum.BLOG_NOT_EXIST, "博客不存在或已被删除");
        }

        // 3. 核心权限校验：仅发布者可删除
        if (!blog.getUserId().equals(userId)) {
            throw new BusinessException(ErrorEnum.BLOG_NO_PERMISSION, "无权限删除他人博客");
        }

        // 4. 数据库：执行逻辑删除（更新delete_flag=1，而非物理删除）
        boolean isDelete = removeById(blogId); // MyBatis-Plus逻辑删除：需提前配置@TableLogic
        if (!isDelete) {
            throw new BusinessException(ErrorEnum.BLOG_DEL_FAIL, "博客删除失败");
        }
        log.info("用户{}成功逻辑删除博客{}", userId, blogId);

        // 5. 同步清理Redis中该博客的所有相关缓存（避免脏数据）
        cleanBlogRedisCache(blogId);
    }

    /**
     * 通用方法：清理指定博客在Redis中的所有相关缓存（删除/下架博客时调用）
     * 1. 清理该博客的点赞ZSet（blog:liked:blogId）
     * 2. 清理所有用户关注流中该博客的ID（feed:userId 中的blogId）
     */
    private void cleanBlogRedisCache(Long id) {
        String blogIdStr = id.toString();
        try {
            // 1. 第一步：删除该博客的点赞ZSet（如blog:liked:101）
            String likedKey = RedisConstant.BLOG_LIKED_KEY + id;
            stringRedisTemplate.delete(likedKey);
            log.info("清理Redis缓存：删除博客{}的点赞ZSet，Key={}", id, likedKey);

            // 2. 第二步：清理所有用户关注流中该博客的ID（feed:* 中的blogIdStr）
            // 2.1 匹配所有关注流Key（feed:1001、feed:1002...）
            Set<String> feedKeys = stringRedisTemplate.keys(RedisConstant.FEED_KEY + "*");
            if (feedKeys != null && !feedKeys.isEmpty()) {
                // 2.2 批量移除所有feedKey中的该博客ID
                feedKeys.forEach(feedKey -> {
                    stringRedisTemplate.opsForZSet().remove(feedKey, blogIdStr);
                });
                log.info("清理Redis缓存：从{}个用户的关注流中移除博客{}", feedKeys.size(), id);
            }
        } catch (Exception e) {
            log.error("清理博客{}的Redis缓存失败，异常信息：{}", id, e.getMessage(), e);
            // 缓存清理失败不抛业务异常（避免影响主业务：博客删除），仅记录日志
        }
    }

    /**
     * 用户编辑自己的博客：属性更新 + 保留不可改字段 + 同步更新时间
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMyBlog(BlogUpdateDTO blogUpdateDTO) {
        // 1. 获取当前登录用户ID，登录校验
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException(ErrorEnum.NO_LOGIN, "请先登录再操作");
        }

        // 2. 从DTO中获取博客ID，查询原博客，非空校验
        Long blogId = blogUpdateDTO.getId();
        Blog originalBlog = getById(blogId);
        if (originalBlog == null) {
            throw new BusinessException(ErrorEnum.BLOG_NOT_EXIST, "博客不存在或已被删除");
        }

        // 3. 核心权限校验：仅发布者可编辑
        if (!originalBlog.getUserId().equals(userId)) {
            throw new BusinessException(ErrorEnum.NO_PERMISSION, "无权限编辑他人博客");
        }

        // 4. 属性拷贝：将DTO的可编辑字段拷贝到原博客，忽略不可改字段（如user_id/create_time/like_count）
        BeanUtil.copyProperties(blogUpdateDTO, originalBlog);
        // 5. 更新修改时间（前端无需传，后端自动维护）
        originalBlog.setUpdateTime(LocalDateTime.now());

        // 6. 数据库：执行更新
        boolean isUpdate = updateById(originalBlog);
        if (!isUpdate) {
            throw new BusinessException(ErrorEnum.BLOG_UPDATE_FAIL, "博客编辑失败");
        }
        log.info("用户{}成功编辑博客{}", userId, blogId);

        // 【可选】如果你的项目做了「博客详情Redis缓存」，这里要更新缓存（你当前项目仅存ID，可忽略）
        // updateBlogDetailRedisCache(originalBlog);

    }
}
