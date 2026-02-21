package com.lghj.controller.notify;

import com.lghj.constant.JwtClaimsConstant;
import com.lghj.enums.UserType2Num;
import com.lghj.pojo.dto.LoginDTO;
import com.lghj.pojo.dto.Result;
import com.lghj.pojo.entity.User;
import com.lghj.pojo.vo.LoginVO;
import com.lghj.properties.JwtProperties;
import com.lghj.service.ILoginService;
import com.lghj.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录登出
 */
@Api(tags = "用户登录接口")
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor // 自动生成包含所有 final 成员变量的构造器，简化代码
public class LoginController {

    private final ILoginService loginService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    public Result login(@RequestBody LoginDTO loginDTO) {
        log.info("用户登录：{}", loginDTO);

        User user = loginService.login(loginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.USER_TYPE, user.getUserType());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        LoginVO loginVO = LoginVO.builder()
                .token(token)
                .id(user.getId())
                .username(user.getUsername())
                .userType(user.getUserType())
                .identityDesc(UserType2Num.getName(user.getUserType()))
                .state(user.getStatus())
                .build();

        return Result.success(loginVO);
    }

    /**
     * 退出
     */
    @PostMapping("/logout")
    @ApiOperation(value = "员工退出")
    public Result logout() {
        return Result.success();
    }
}
