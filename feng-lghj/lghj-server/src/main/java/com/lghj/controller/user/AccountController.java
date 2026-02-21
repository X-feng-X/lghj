package com.lghj.controller.user;

import com.lghj.context.BaseContext;
import com.lghj.pojo.dto.Result;
import com.lghj.pojo.entity.SimAccount;
import com.lghj.pojo.entity.UserPosition;
import com.lghj.service.IAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/account")
@Api(tags = "账户管理接口")
@Slf4j
@RequiredArgsConstructor
public class AccountController {

    private final IAccountService accountService;

    @PostMapping("/create")
    @ApiOperation("创建模拟账户")
    public Result createAccount() {
        Long userId = BaseContext.getCurrentId();
        log.info("创建模拟账户: userId={}", userId);
        SimAccount account = accountService.createAccount(userId);
        return Result.success(account);
    }

    @GetMapping("/query_info")
    @ApiOperation("获取账户信息")
    public Result getAccountInfo() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取账户信息: userId={}", userId);
        SimAccount account = accountService.getAccountByUserId(userId);
        return account != null ? Result.success(account) : Result.error("账户不存在");
    }

    @GetMapping("/query_positions")
    @ApiOperation("获取持仓列表")
    public Result getPositions() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取持仓列表: userId={}", userId);
        List<UserPosition> positions = accountService.getUserPositions(userId);
        return Result.success(positions);
    }

    @GetMapping("/query_position")
    @ApiOperation("获取特定股票持仓")
    public Result getPosition(@RequestParam String symbol) {
        Long userId = BaseContext.getCurrentId();
        log.info("获取特定股票持仓: userId={}, symbol={}", userId, symbol);
        UserPosition position = accountService.getUserPositionBySymbol(userId, symbol);
        return position != null ? Result.success(position) : Result.error("持仓不存在");
    }
}
