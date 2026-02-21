package com.lghj.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


/**
 * 基于redis的全局唯一id生成器
 */
@Component
public class RedisIdWorker {

    /**
     * 开始时间戳
     */
    private static final long BEGIN_TIMESTAMP = 1640995200L;
    /**
     * 序列号位数
     */
    private static final int COUNT_BITS = 32;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public long nextId(String keyPrefix) {
        // 1. 生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 2. 生成序列号
        // 2.1 获取当前日期，精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2.2 自增长
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 3. 拼接并返回
        return timestamp << COUNT_BITS | count;
        /*
            << : 左移运算符
            | : 按位或运算符
            << (左移运算符) 和 | (按位或运算符) 是专门用于二进制位运算的运算符
            这些运算符在十进制系统中是没有意义的，所以是二进制运算
            
            // 第一步：时间戳左移32位
            timestamp << COUNT_BITS
            // 1000 << 32
            // 结果：1000后面跟上32个0
            // 二进制形式：1111101000 00000000000000000000000000000000
            
            // 第二步：与序列号进行或运算
            (timestamp << COUNT_BITS) | count
            // 1111101000 00000000000000000000000000000000
            // |
            // 0000000000 00000000000000000000000000000101
            // =
            // 1111101000 00000000000000000000000000000101

         */
    }
}