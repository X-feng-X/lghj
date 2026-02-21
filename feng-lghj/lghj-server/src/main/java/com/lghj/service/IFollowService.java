package com.lghj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lghj.pojo.entity.Follow;

public interface IFollowService extends IService<Follow> {
    void follow(Long followUserId, Boolean isFollow);

    Boolean isFollow(Long followUserId);
}
