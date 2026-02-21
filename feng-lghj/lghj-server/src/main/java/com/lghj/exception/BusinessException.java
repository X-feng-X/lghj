package com.lghj.exception;

import com.lghj.enums.ErrorEnum;
import lombok.Getter;

/**
 * 自定义业务异常类（仅用于抛出业务相关错误，如用户名重复、用户不存在等）
 */
@Getter
public class BusinessException extends RuntimeException {

    // getter方法（用于全局异常处理器获取错误枚举）
    // 携带错误枚举
    private final ErrorEnum errorEnum;

    // 构造器（接收错误枚举）
    public BusinessException(ErrorEnum errorEnum) {
        super(errorEnum.getMsg()); // 调用父类构造器，传入错误信息（便于日志打印）
        this.errorEnum = errorEnum;
    }

    // 构造器（接收错误枚举 + 详细信息）
    public BusinessException(ErrorEnum errorEnum, String detailMsg) {
        super(errorEnum.getMsg() + "：" + detailMsg);
        this.errorEnum = errorEnum;
    }

}