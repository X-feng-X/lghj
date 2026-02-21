package com.lghj.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lghj.pojo.dto.Result;
import com.lghj.pojo.entity.StockBasic;
import com.lghj.service.IStockSearchService;
import com.lghj.service.IStockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

@Api(tags = "管理端-导入A股基础信息接口")
@RestController
@RequestMapping("/api/admin/stock")
public class StockController {

    @Resource
    private IStockService stockService; // 注入接口
    @Resource
    private IStockSearchService stockSearchService;

    /**
     * 初始化导入A股基础信息
     */
    @PostMapping("/import")
    @ApiOperation("初始化导入A股基础信息")
    public Result importData() {
        String msg = stockService.importStockBasic();
        return Result.success(msg);
    }

    /**
     * 同步数据到Elasticsearch
     */
    @PostMapping("/sync-es")
    @ApiOperation("同步数据到Elasticsearch")
    public Result syncToEs() {
        stockSearchService.syncData();
        return Result.success("同步完成");
    }

    @GetMapping("/page")
    @ApiOperation("分页查询股票列表")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String keyword) {
        Page<StockBasic> page = stockService.pageQuery(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    /**
     * 根据股票代码更新股票信息-单条更新
     */
    @PutMapping("/update")
    @ApiOperation("根据股票代码更新股票信息-单条更新")
    public Result updateStock(@RequestBody StockBasic stockBasic) {
        boolean success = stockService.updateByCode(stockBasic);
        return success ? Result.success("更新成功") : Result.error("更新失败（股票不存在或更新失败）");
    }

    /**
     * 根据股票代码更新股票信息-批量更新
     */
    // TODO 在fastapi中将excel写到特定位置
    @PostMapping("/batch-update")
    @ApiOperation("根据股票代码更新股票信息-批量更新")
    public String batchUpdate() throws IOException {
        stockService.batchUpdateFromExcel();
        return "批量更新任务已提交";
    }
}