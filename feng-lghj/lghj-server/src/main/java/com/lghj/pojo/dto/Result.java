package com.lghj.pojo.dto;

import com.lghj.enums.ErrorEnum;
import lombok.Data;

/**
 * 统一返回结果类（对接错误枚举）
 */
@Data
public class Result {
    // 响应状态码（200=成功，其他=失败）
    private Integer code;
    // 响应提示信息
    private String msg;
    // 响应数据（成功时返回，失败时可null）
    private Object data;

    // ====================== 私有构造器（外部通过静态方法创建） ======================
    private Result() {
    }

    private Result(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ====================== 成功响应（重载方法） ======================
    public static Result success() {
        return new Result(ErrorEnum.SUCCESS.getCode(), ErrorEnum.SUCCESS.getMsg(), null);
    }

    public static Result success(Object data) {
        return new Result(ErrorEnum.SUCCESS.getCode(), ErrorEnum.SUCCESS.getMsg(), data);
    }

    public static Result success(String msg) {
        return new Result(ErrorEnum.SUCCESS.getCode(), msg, null);
    }

    // ====================== 失败响应（对接错误枚举，核心优化） ======================
    public static Result error(String msg) {
        return new Result(500, msg, null);
    }

    public static Result error(ErrorEnum errorEnum) {
        return new Result(errorEnum.getCode(), errorEnum.getMsg(), null);
    }

    public static Result error(ErrorEnum errorEnum, String detailMsg) {
        // 拼接详细错误信息（枚举默认信息 + 自定义详情）
        String msg = errorEnum.getMsg() + "：" + detailMsg;
        return new Result(errorEnum.getCode(), msg, null);
    }
}