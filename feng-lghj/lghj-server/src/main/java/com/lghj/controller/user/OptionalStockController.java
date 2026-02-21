package com.lghj.controller.user;

import com.lghj.context.BaseContext;
import com.lghj.pojo.dto.Result;
import com.lghj.pojo.vo.StockFollowVO;
import com.lghj.service.IUserStockFollowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/optional")
@Api(tags = "自选股管理接口")
@Slf4j
@RequiredArgsConstructor
public class OptionalStockController {

    private final IUserStockFollowService userStockFollowService;

    @PostMapping("/add")
    @ApiOperation("添加自选股")
    public Result add(@RequestParam String symbol) {
        Long userId = BaseContext.getCurrentId();
        log.info("添加自选股: userId={}, symbol={}", userId, symbol);
        boolean success = userStockFollowService.addFollow(userId, symbol);
        return success ? Result.success() : Result.error("添加失败，可能股票不存在");
    }

    @PostMapping("/remove")
    @ApiOperation("删除自选股")
    public Result remove(@RequestParam String symbol) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除自选股: userId={}, symbol={}", userId, symbol);
        boolean success = userStockFollowService.removeFollow(userId, symbol);
        return success ? Result.success() : Result.error("删除失败");
    }

    @GetMapping("/list")
    @ApiOperation("获取自选股列表")
    public Result list() {
        Long userId = BaseContext.getCurrentId();
        log.info("获取自选股列表: userId={}", userId);
        List<StockFollowVO> list = userStockFollowService.getUserFollowList(userId);
        return Result.success(list);
    }
}
