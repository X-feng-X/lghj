package com.lghj.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lghj.pojo.dto.Result;
import com.lghj.pojo.entity.TradeDeal;
import com.lghj.pojo.entity.TradeOrder;
import com.lghj.service.ITradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端-交易管理接口")
@Slf4j
@RestController
@RequestMapping("/api/admin/trade")
@RequiredArgsConstructor
public class TradeManageController {

    private final ITradeService tradeService;

    /**
     * 分页查询委托单
     */
    @GetMapping("/order/page")
    @ApiOperation("分页查询委托单")
    public Result orderPage(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(required = false) Long userId,
                            @RequestParam(required = false) String symbol) {
        Page<TradeOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(TradeOrder::getUserId, userId);
        }
        if (StringUtils.hasText(symbol)) {
            wrapper.eq(TradeOrder::getSymbol, symbol);
        }
        // 按创建时间倒序
        wrapper.orderByDesc(TradeOrder::getCreateTime);
        
        page = tradeService.page(page, wrapper);
        
        if (page.getRecords() == null) {
            page.setRecords(List.of());
        }
        
        return Result.success(page);
    }

    /**
     * 分页查询成交记录
     */
    @GetMapping("/deal/page")
    @ApiOperation("分页查询成交记录")
    public Result dealPage(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) Long userId,
                           @RequestParam(required = false) String symbol) {
        Page<TradeDeal> page = tradeService.queryDealPage(pageNum, pageSize, userId, symbol);
        
        if (page.getRecords() == null) {
            page.setRecords(List.of());
        }
        
        return Result.success(page);
    }
}
