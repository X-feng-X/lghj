package com.lghj.controller.user;

import com.lghj.pojo.doc.StockDoc;
import com.lghj.pojo.dto.Result;
import com.lghj.service.IStockSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/stock")
@Api(tags = "股票搜索接口")
@Slf4j
@RequiredArgsConstructor
public class StockSearchController {

    private final IStockSearchService stockSearchService;

    @GetMapping("/search")
    @ApiOperation("搜索股票（支持代码前缀和名称模糊搜索）")
    public Result search(@RequestParam String keyword) {
        log.info("搜索股票关键词: {}", keyword);
        List<StockDoc> result = stockSearchService.search(keyword);
        return Result.success(result);
    }
}
