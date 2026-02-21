package com.lghj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lghj.constant.LikeStatusConstant;
import com.lghj.constant.RedisConstant;
import com.lghj.context.BaseContext;
import com.lghj.enums.ErrorEnum;
import com.lghj.exception.BusinessException;
import com.lghj.mapper.BlogCommentsMapper;
import com.lghj.pojo.dto.*;
import com.lghj.pojo.entity.Blog;
import com.lghj.pojo.entity.BlogComments;
import com.lghj.pojo.entity.User;
import com.lghj.pojo.vo.BlogCommentVO;
import com.lghj.service.IBlogCommentsService;
import com.lghj.service.IBlogService;
import com.lghj.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 博客评论区
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

    private final StringRedisTemplate stringRedisTemplate;
    private final IUserService userService;
    private final IBlogService blogService;

    /**
     * 新增评论：一级/二级通用，二级评论需校验父评论存在
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(BlogCommentAddDTO dto) {
        // 1. 登录校验
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException(ErrorEnum.NO_LOGIN);
        }

        // 2. 参数校验：二级评论需校验父评论存在（且是一级评论、状态正常、未删除）
        Long parentId = dto.getParentId();
        if (parentId > 0) {
            BlogComments parentComment = getById(parentId);
            if (parentComment == null || parentComment.getParentId() != 0 ||
                    parentComment.getStatus() == 0 || parentComment.getIsDeleted() == 1) {
                throw new BusinessException(ErrorEnum.BLOG_PARENT_COMMENT_NOT_FOUND, "父评论不存在或已被禁用/删除");
            }
            // 二级评论需与父评论归属同一博客
            if (!parentComment.getBlogId().equals(dto.getBlogId())) {
                throw new BusinessException(ErrorEnum.BLOG_COMMENT_MISMATCHING, "二级评论与父评论不属于同一博客");
            }
        }

        // 3. 封装评论实体
        BlogComments comment = BeanUtil.copyProperties(dto, BlogComments.class);
        comment.setUserId(userId); // 设置评论发布者ID
        comment.setStatus((short) 1); // 默认为正常状态
        comment.setLiked(0); // 初始点赞数为0

        // 4. 保存到数据库
        boolean save = save(comment);
        if (!save) {
            throw new BusinessException(ErrorEnum.BLOG_COMMENT_SAVE_FAIL, "评论发表失败，请稍后再试");
        }
        log.info("用户{}成功发表评论，评论ID：{}，博客ID：{}", userId, comment.getId(), dto.getBlogId());
    }

    /**
     * 查询评论列表：先查一级评论（分页），再批量查二级评论，组装树形结构，补充用户信息+点赞状态
     */
    @Override
    public PageResult queryCommentList(BlogCommentQueryDTO dto) {
        Long blogId = dto.getBlogId();
        Integer pageNum = dto.getPageNum();
        Integer pageSize = dto.getPageSize();
        Long currentUserId = BaseContext.getCurrentId() == null ? null : BaseContext.getCurrentId();

        // 1. 分页查询一级评论（parent_id=0）
        Long start = (long) (pageNum - 1) * pageSize;
        IPage<BlogComments> page = new Page<>(pageNum, pageSize);
        IPage<BlogComments> firstLevelPage = lambdaQuery()
                .eq(BlogComments::getBlogId, blogId)
                .eq(BlogComments::getParentId, 0) // 一级评论
                .eq(BlogComments::getStatus, 1)    // 正常状态
                .eq(BlogComments::getIsDeleted, 0)// 未删除
                .orderByDesc(BlogComments::getCreateTime)
                .page(page);

        List<BlogComments> firstLevelComments = firstLevelPage.getRecords();
        if (CollectionUtils.isEmpty(firstLevelComments)) {
            return new PageResult<>(0L, 0, pageNum, pageSize, Collections.emptyList());
        }

        // 2. 自定义：批量查二级评论（避免N+1）
        List<Long> parentIds = firstLevelComments.stream().map(BlogComments::getId).collect(Collectors.toList());
        List<BlogComments> secondLevelComments = lambdaQuery()
                .eq(BlogComments::getBlogId, blogId)
                .in(BlogComments::getParentId, parentIds)
                .eq(BlogComments::getStatus, 1)
                .eq(BlogComments::getIsDeleted, 0)
                .orderByAsc(BlogComments::getCreateTime)
                .list();
        Map<Long, List<BlogComments>> secondLevelMap = secondLevelComments.stream()
                .collect(Collectors.groupingBy(BlogComments::getParentId));

        // 3. 自定义：组装VO（含用户信息、点赞状态、二级评论）
        List<BlogCommentVO> blogCommentVOList = assembleCommentVO(firstLevelComments, secondLevelMap, currentUserId);

        // 4. 封装分页结果返回
        PageResult<BlogCommentVO> pageResult = new PageResult<>(
                firstLevelPage.getTotal(), // 总条数（IPage自动计算）
                (int) firstLevelPage.getPages(), // 总页数（IPage自动计算）
                pageNum,
                pageSize,
                blogCommentVOList
        );
        return pageResult;

    }

    /**
     * 自定义：组装VO的私有方法
     */
    private List<BlogCommentVO> assembleCommentVO(List<BlogComments> firstLevelComments,
                                                  Map<Long, List<BlogComments>> secondLevelMap,
                                                  Long currentUserId) {

        // 1. 批量查用户信息 - 【将用户信息封装为一个Map<用户id, 用于评论展示的用户信息>】
        Set<Long> userIds = new HashSet<>();
        firstLevelComments.forEach(c -> userIds.add(c.getUserId())); // 使用 Set 是为了自动去重，避免同一个用户id出现多次。
        secondLevelMap.values().forEach(list -> list.forEach(c -> userIds.add(c.getUserId())));
        List<User> users = userService.listByIds(userIds); // 查询所有评论的用户的信息（无论一级二级）
        Map<Long, UserBlogCommentsMessageDTO> userDTOMap = users.stream()
                .map(user -> BeanUtil.copyProperties(user, UserBlogCommentsMessageDTO.class))
                .collect(Collectors.toMap(UserBlogCommentsMessageDTO::getId, u -> u));

        // 2. 组装一级评论VO + 二级评论VO
        List<BlogCommentVO> voList = new ArrayList<>();
        for (BlogComments first : firstLevelComments) {
            BlogCommentVO firstVO = BeanUtil.copyProperties(first, BlogCommentVO.class); // 将原本查到的BlogComments类型映射为BlogCommentVO
            firstVO.setUser(userDTOMap.get(first.getUserId())); // 用userDTOMap通过用户的id得到他的UserBlogCommentsMessageDTO
            firstVO.setIsLiked(getLikedStatus(first.getId(), currentUserId)); // 判断并封装是否点赞

            // 挂载二级评论
            List<BlogComments> secondList = secondLevelMap.get(first.getId()); // 拿到二级评论的用户id
            if (!CollectionUtils.isEmpty(secondList)) {
                List<BlogCommentVO> secondVOList = secondList.stream().map(second -> {
                    BlogCommentVO secondVO = BeanUtil.copyProperties(second, BlogCommentVO.class); // 映射
                    secondVO.setUser(userDTOMap.get(second.getUserId())); // 封装UserBlogCommentsMessageDTO对象
                    secondVO.setIsLiked(getLikedStatus(second.getId(), currentUserId)); // 判断并封装是否点赞
                    return secondVO;
                }).collect(Collectors.toList());
                firstVO.setChildren(secondVOList);
            }
            voList.add(firstVO);
        }
        return voList;
    }

    /**
     * 评论点赞/取消点赞：Redis记录点赞状态，数据库原子更新点赞数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Long commentId) {
        // 1. 登录校验
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException(ErrorEnum.NO_LOGIN);
        }
        String likedKey = RedisConstant.BLOG_COMMENT_LIKED_KEY + commentId;

        // 2. 校验评论是否存在（正常状态、未删除）
        BlogComments comment = getById(commentId);
        if (comment == null || comment.getStatus() == 0 || comment.getIsDeleted() == 1) {
            throw new BusinessException(ErrorEnum.BLOG_COMMENT_NOT_FOUND, "评论不存在或已被禁用/删除");
        }

        // 3. 判断当前用户是否已点赞
        Boolean isLiked = stringRedisTemplate.opsForSet().isMember(likedKey, userId.toString()); // TODO 可以用私有方法
        if (Boolean.TRUE.equals(isLiked)) {
            // 已点赞：取消点赞，Redis移除用户ID，数据库点赞数-1
            stringRedisTemplate.opsForSet().remove(likedKey, userId.toString());
            lambdaUpdate().set(BlogComments::getLiked, comment.getLiked() - 1).eq(BlogComments::getId, commentId).update();
            log.info("用户{}取消点赞评论{}", userId, commentId);
        } else {
            // 未点赞：点赞，Redis添加用户ID，数据库点赞数+1
            stringRedisTemplate.opsForSet().add(likedKey, userId.toString());
            lambdaUpdate().set(BlogComments::getLiked, comment.getLiked() + 1).eq(BlogComments::getId, commentId).update();
            log.info("用户{}点赞评论{}", userId, commentId);
        }
    }

    /**
     * 删除评论：逻辑删除，权限校验（评论发布者/博客作者/管理员）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        // 1. 登录校验
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException(ErrorEnum.NO_LOGIN);
        }

        // 2. 校验评论是否存在
        BlogComments comment = getById(commentId);
        if (comment == null || comment.getIsDeleted() == 1) {
            throw new BusinessException(ErrorEnum.BLOG_COMMENT_NOT_FOUND, "评论不存在或已被删除");
        }
        Long blogId = comment.getBlogId();
        Long commentAuthorId = comment.getUserId();

        // 3. 权限校验：评论发布者 || 博客作者
        Blog blog = blogService.getById(blogId);
        if (blog == null) {
            throw new BusinessException(ErrorEnum.BLOG_NOT_EXIST, "评论所属博客不存在");
        }

        List<Long> needCleanCommentIds = new ArrayList<>(); // 需清理缓存的评论ID集合
        if (comment.getParentId() == 0) { // 是一级评论，级联删二级评论
            // 3.1 查询该一级评论下的所有二级评论（未删除、正常状态）
            List<BlogComments> secondLevelComments = lambdaQuery()
                    .eq(BlogComments::getParentId, commentId) // 父ID=一级评论ID
                    .eq(BlogComments::getBlogId, blogId)      // 同博客
                    .eq(BlogComments::getIsDeleted, 0)        // 未删除
                    .list();

            if (!CollectionUtils.isEmpty(secondLevelComments)) {
                // 3.2 提取二级评论ID，用于批量删除+缓存清理
                List<Long> secondLevelIds = secondLevelComments.stream() // 得到所有二级评论的id
                        .map(BlogComments::getId)
                        .collect(Collectors.toList());
                needCleanCommentIds.addAll(secondLevelIds); // 将要删除的二级评论的id加入needCleanCommentIds列表中

                // 3.3 批量逻辑删除二级评论（MyBatis-Plus批量更新）
                boolean deleteSecond = removeByIds(secondLevelIds);
                if (!deleteSecond) {
                    throw new BusinessException(ErrorEnum.BLOG_COMMENT_DELETE_FAIL, "二级评论删除失败，操作终止");
                }
                log.info("级联删除一级评论{}的所有二级评论，共{}条", commentId, secondLevelIds.size());
            }
        }

        // 4. 逻辑删除评论（MyBatis-Plus自动处理is_deleted=1）
        needCleanCommentIds.add(commentId); // 加入当前评论ID，后续统一清理缓存
        boolean removeCurrent = removeById(commentId);
        if (!removeCurrent) {
            throw new BusinessException(ErrorEnum.BLOG_COMMENT_DELETE_FAIL, "评论删除失败，请稍后再试");
        }

        // 5. 清理Redis点赞缓存
        needCleanCommentIds.forEach(commentIdToClean -> {
            stringRedisTemplate.delete(RedisConstant.BLOG_COMMENT_LIKED_KEY + commentIdToClean);
        });
        log.info("批量清理Redis缓存：共清理{}条评论的点赞Key，评论ID：{}", needCleanCommentIds.size(), needCleanCommentIds);
    }

    /**
     * 私有方法：获取当前用户对指定评论的点赞状态
     */
    private Integer getLikedStatus(Long commentId, Long userId) {
        if (userId == null) {
            return LikeStatusConstant.UNLIKED_STATUS;
        }
        String likedKey = RedisConstant.BLOG_COMMENT_LIKED_KEY + commentId;
        Boolean isLiked = stringRedisTemplate.opsForSet().isMember(likedKey, userId.toString());
        return Boolean.TRUE.equals(isLiked) ? LikeStatusConstant.LIKED_STATUS : LikeStatusConstant.UNLIKED_STATUS;
    }
}
