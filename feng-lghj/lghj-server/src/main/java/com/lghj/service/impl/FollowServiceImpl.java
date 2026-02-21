package com.lghj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lghj.constant.RedisConstant;
import com.lghj.context.BaseContext;
import com.lghj.mapper.FollowMapper;
import com.lghj.pojo.entity.Follow;
import com.lghj.service.IFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 关注和取关
     */
    @Override
    public void follow(Long followUserId, Boolean isFollow) {
        // 1. 获取登录用户
        Long userId = BaseContext.getCurrentId();
        String key = RedisConstant.FOLLOWS_KEY + userId;

        // 1. 判断到底是关注还是取关
        if (isFollow) {
            // 2. 关注，新增数据
            Follow follow = Follow.builder().userId(userId).followUserId(followUserId).build();
            boolean isSuccess = save(follow);
            if (isSuccess) {
                // 把关注用户的id，放入redis的set集合 sadd userId followUserId
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            // 3. 取关，删除 delete from tb_follow where user_id = ? and follow_user_id = ?
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId)
                    .eq("follow_user_id", followUserId));
            if (isSuccess) {
                // 把关注的用户id从redis集合中移除
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
    }

    /**
     * 判断是否关注
     */
    @Override
    public Boolean isFollow(Long followUserId) {
        // 1. 获取登录用户
        Long userId = BaseContext.getCurrentId();
        // 2. 查询是否关注 select count(*) form tb_follow where user_id = ? and follow_user_id = ?
        Integer count = query().eq("user_id", userId)
                .eq("follow_user_id", followUserId).count();
        // 3. 判断
        return count > 0;
        // TODO 我觉得可以用Redis啊，应该会更快
    }
}
