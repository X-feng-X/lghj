package com.lghj.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lghj.pojo.dto.PageResult;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 转换PageResult格式工具类
 */
public class PageResultConvertUtil {
    
    /**
     * 将 MyBatis-Plus 的 IPage 转换为 PageResult
     */
    public static <T> PageResult<T> convert(IPage<T> page) {
        return PageResult.<T>builder()
            .total(page.getTotal())
            .totalPage((int) page.getPages())
            .pageNum((int) page.getCurrent())
            .pageSize((int) page.getSize())
            .list(page.getRecords())
            .build();
    }
    
    /**
     * 将 MyBatis-Plus 的 IPage<S> 转换为 PageResult<T>
     */
    public static <S, T> PageResult<T> convert(IPage<S> page, Function<S, T> converter) {
        List<T> convertedList = page.getRecords()
            .stream()
            .map(converter)
            .collect(Collectors.toList());
        
        return PageResult.<T>builder()
            .total( page.getTotal())
            .totalPage((int) page.getPages())
            .pageNum((int) page.getCurrent())
            .pageSize((int) page.getSize())
            .list(convertedList)
            .build();
    }
}