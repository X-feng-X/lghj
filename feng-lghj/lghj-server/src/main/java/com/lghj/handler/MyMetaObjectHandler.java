package com.lghj.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.lghj.context.BaseContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充器
 * 自动填充创建时间、更新时间、创建人、更新人字段
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        // 填充创建时间、更新时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        // 填充创建人、更新人（当前登录用户ID，未登录为null）
        Long userId = BaseContext.getCurrentId() == null ? null : BaseContext.getCurrentId();
        this.strictInsertFill(metaObject, "createUser", () -> userId, Long.class);
        this.strictInsertFill(metaObject, "updateUser", () -> userId, Long.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        // 填充更新人（当前登录用户ID）
        Long userId = BaseContext.getCurrentId() == null ? null : BaseContext.getCurrentId();
        this.strictUpdateFill(metaObject, "updateUser", () -> userId, Long.class);
    }
}