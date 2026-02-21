package com.lghj.enums;

import lombok.Getter;

/**
 * 全局错误枚举类（统一管理所有错误码和错误信息）
 * 错误码规范：
 * 1. 前缀区分错误类型：PARAM_（参数错误）、BUSINESS_（业务错误）、SYSTEM_（系统错误）
 * 2. 错误码为数字，便于前端判断处理（如 10001=参数为空，20001=用户名已存在）
 */
@Getter // 提供getter方法，便于获取错误码和错误信息
public enum ErrorEnum {
    // ====================== 通用错误 ======================
    SUCCESS(200, "操作成功"),
    NO_LOGIN(401, "未登录，无法访问"),
    SYSTEM_ERROR(500, "系统内部异常，请稍后重试"),

    // ====================== 参数错误（10000开头） ======================
    PARAM_NULL(10001, "参数不能为空"),
    PARAM_INVALID(10002, "参数格式无效"),
    USERNAME_EMPTY(10003, "用户名不能为空"),

    // ====================== 用户管理（20000开头） ======================
    USERNAME_EXIST(20001, "用户名已存在，无法新增"),
    USER_NOT_EXIST(20002, "用户不存在，无法操作"),
    USER_STATUS_INVALID(20003, "用户状态无效，只能是0（禁用）或1（启用）"),
    USER_SAVE_FAIL(20004, "用户新增失败，数据库操作异常"),
    USER_ADD_FAIL(20005, "新增用户失败"),
    USER_REMOVE_FAIL(20006, "删除用户失败"),
    USER_SELECT_BY_ID(20006, "根据id查询用户失败"),
    USER_UPDATE_FAIL(2007, "修改用户失败"),
    USER_STATE_EX_FAIL(20008, "修改账号状态失败"),
    USER_NOT_FOUND(20009, "账号不存在"),
    PASSWORD_ERROR(20010, "密码错误"),
    ACCOUNT_LOCKED(20011, "账号被锁定"),
    NO_PERMISSION(20012, "无管理员权限，无法访问该接口"),

    // ====================== 博客管理（30000开头） ======================
    BLOG_SAVE_FAIL(30001, "新增博客失败"),
    BLOG_NOT_EXIST(30002, "博客不存在"),
    BLOG_NO_PERMISSION(30003, "无权限删除他人博客"),
    BLOG_DEL_FAIL(30004, "博客删除失败"),
    BLOG_UPDATE_FAIL(30005, "博客编辑失败"),
    BLOG_PARENT_COMMENT_NOT_FOUND(30006, "父评论不存在或已被禁用/删除"),
    BLOG_COMMENT_MISMATCHING(30007, "二级评论与父评论不属于同一博客"),
    BLOG_COMMENT_SAVE_FAIL(30008, "评论发表失败，请稍后再试"),
    BLOG_COMMENT_NOT_FOUND(30009, "评论不存在或已被禁用/删除"),
    BLOG_COMMENT_DELETE_FAIL(30010, "评论删除失败，请稍后再试"),
    NO_FOLLOW_OTHERS(30011, "暂未关注任何用户"),

    // ====================== 股票数据管理（40000开头） ======================
    STOCK_QUERY_FAIL(40001, "获取股票数据失败"),

    // ====================== 交易管理（50000开头） ======================
    DONT_HAVE_ENOUGH_MONEY(50001, "账户可用资金不足"),
    POSITION_NOT_ENOUGH(50002, "持仓不足"),
    POSITION_UPDATE_FAIL(50003,"更新持仓失败，可能存在并发操作"),
    ACCOUNT_UPDATE_FAIL(50004,"更新账户失败，可能存在并发操作"),
    ACCOUNT_NOT_FOUND(50005,"用户账户不存在")


    // ====================== 后续可添加更多业务错误 ======================
    ;

    // 错误码
    private final Integer code;
    // 错误信息
    private final String msg;

    // 枚举构造器（私有，只能内部使用）
    ErrorEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}