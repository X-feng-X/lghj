package com.lghj.controller.user;


import com.lghj.pojo.dto.Result;
import com.lghj.service.IFollowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 关注接口
 */
@Api(tags = "关注接口")
@Slf4j
@RestController
@RequestMapping("/api/user/follow")
@RequiredArgsConstructor // 自动生成包含所有 final 成员变量的构造器，简化代码
public class FollowController {

    private final IFollowService followService;

    /**
     * 关注和取关
     */
    @PutMapping("/{id}/{isFollow}")
    @ApiOperation("关注和取关")
    public Result follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow) {
        followService.follow(followUserId, isFollow);
        return Result.success();
    }

    /**
     * 判断是否关注
     */
    @GetMapping("/or/not/{id}")
    @ApiOperation("判断是否关注")
    public Result follow(@PathVariable("id") Long followUserId) {
        Boolean result = followService.isFollow(followUserId);
        return Result.success(result);
    }
}
