package com.lghj.constant;

/**
 * redis常量类
 */
public class RedisConstant {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;
    /**
     * 关注的用户Key前缀
     */
    public static final String FOLLOWS_KEY = "follows:";
    /**
     * 博客点赞Key前缀：blog:comment:{id}
     */
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    /**
     * 关注的博主的博客Key前缀：feed:{followUserId}
     */
    public static final String FEED_KEY = "feed:";
    /**
     * 博客评论点赞Key前缀：blog:comment:liked:{commentId}
     */
    public static final String BLOG_COMMENT_LIKED_KEY = "blog:comment:liked:";
    /**
     * 股票实时数据缓存Key前缀
     */
    public static final String STOCK_REAL_TIME_KEY = "stock:realtime:";
    /**
     * 用户自选股Key前缀：user:stock:follow:{userId}
     */
    public static final String REDIS_FOLLOW_PREFIX = "user:stock:follow:";
    /**
     * 缓存锁前缀，防止缓存击穿lock:stock:minute:{market+code}
     */
    public static final String REDIS_LOCK_PREFIX = "lock:stock:minute:";
    /**
     * 股票预测结果Key前缀stock:prediction:{stock}
     */
    public static final String STOCK_PREDICTION = "stock:prediction";

}
