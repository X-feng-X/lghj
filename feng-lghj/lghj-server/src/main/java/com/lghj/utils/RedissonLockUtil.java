package com.lghj.utils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedissonLockUtil {

    private final RedissonClient redissonClient;

    @Autowired
    public RedissonLockUtil(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 获取分布式锁
     *
     * @param lockKey   锁键
     * @param waitTime  等待时间（秒）
     * @param leaseTime 持有时间（秒）
     * @return 是否获取到锁
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean locked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            log.info("尝试获取分布式锁，锁键：{}，结果：{}", lockKey, locked);
            return locked;
        } catch (InterruptedException e) {
            log.error("获取分布式锁失败，锁键：{}", lockKey, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey 锁键
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("释放分布式锁，锁键：{}", lockKey);
            }
        } catch (Exception e) {
            log.error("释放分布式锁失败，锁键：{}", lockKey, e);
        }
    }

    /**
     * 获取分布式锁（无限等待）
     *
     * @param lockKey   锁键
     * @param leaseTime 持有时间（秒）
     */
    public void lock(String lockKey, long leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.lock(leaseTime, TimeUnit.SECONDS);
            log.info("获取分布式锁，锁键：{}", lockKey);
        } catch (Exception e) {
            log.error("获取分布式锁失败，锁键：{}", lockKey, e);
        }
    }

    /**
     * 生成交易锁键
     *
     * @param userId 用户ID
     * @param symbol 股票代码
     * @return 锁键
     */
    public static String generateTradeLockKey(Long userId, String symbol) {
        return "trade:lock:" + userId + ":" + symbol;
    }

    /**
     * 生成账户锁键
     *
     * @param userId 用户ID
     * @return 锁键
     */
    public static String generateAccountLockKey(Long userId) {
        return "account:lock:" + userId;
    }
}
