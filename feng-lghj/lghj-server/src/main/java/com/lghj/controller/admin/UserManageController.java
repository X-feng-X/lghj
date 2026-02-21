package com.lghj.controller.admin;

import com.lghj.enums.ErrorEnum;
import com.lghj.pojo.dto.*;
import com.lghj.pojo.vo.UserVO;
import com.lghj.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户管理
 */
@Api(tags = "管理端-用户管理接口")
@Slf4j
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor // 自动生成包含所有 final 成员变量的构造器，简化代码
public class UserManageController {

    private final IUserService userService;

    /**
     * 分页条件查询用户接口
     */
    @GetMapping
    @ApiOperation(value = "分页条件查询用户接口")
    public Result getUserByIds(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            UserQueryDTO userQueryDTO) {
        log.info("分页条件查询用户，参数：{}", userQueryDTO);
        PageResult<UserVO> userPage = userService.queryUserPage(current, size, userQueryDTO);
        return Result.success(userPage);
    }

    /**
     * 新增用户接口
     */
    @PostMapping("/add")
    @ApiOperation("新增用户接口")
    public Result addUser(@Valid @RequestBody UserDTO userDTO) {
        log.info("接收新增用户请求，参数：{}", userDTO);
        boolean success = userService.addUser(userDTO);
        return success ? Result.success() : Result.error(ErrorEnum.USER_ADD_FAIL);
    }

    /**
     * 删除用户接口
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除用户接口")
    public Result removeUser(@ApiParam("用户id") @PathVariable Long id) {
        log.info("删除用户：{}", id);
        boolean success = userService.removeUser(id);
        return success ? Result.success() : Result.error(ErrorEnum.USER_REMOVE_FAIL);
    }

    /**
     * 根据id查询用户接口
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询用户接口")
    public Result getUserById(@ApiParam("用户id") @PathVariable("id") Long id) {
        log.info("根据id查询用户：{}", id);
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

    /**
     * 更改用户信息接口
     */
    @PutMapping("/update")
    @ApiOperation("更改用户信息接口")
    public Result updateUser(@RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("修改用户信息，参数：{}", userUpdateDTO);
        boolean success = userService.updateUser(userUpdateDTO);
        return success ? Result.success() : Result.error(ErrorEnum.USER_UPDATE_FAIL);
    }

    /**
     * 启用禁用用户账号
     */
    @PostMapping("/changeStatus/{id}")
    @ApiOperation("启用禁用用户账号")
    public Result startOrStop(@PathVariable Long id, @RequestParam Short status) {
        log.info("启用禁用用户账号：id:{}、状态:{}", id, status);
        boolean success = userService.changeUserStatus(id, status);
        String msg = status == 1 ? "启用账号成功" : "禁用账号成功";
        return success ? Result.success(msg) : Result.error(ErrorEnum.USER_STATE_EX_FAIL);
    }
}
