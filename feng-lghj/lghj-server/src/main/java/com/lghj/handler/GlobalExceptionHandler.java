package com.lghj.handler;

import com.lghj.enums.ErrorEnum;
import com.lghj.exception.BusinessException;
import com.lghj.pojo.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器（统一捕获所有异常，返回统一格式Result）
 */
@Slf4j
@RestControllerAdvice // 全局捕获@RestController的异常
public class GlobalExceptionHandler {

    // ====================== 捕获自定义业务异常（优先级最高） ======================
    @ExceptionHandler(BusinessException.class) // 捕获并处理系统中抛出的 BusinessException 业务异常
    public Result handleBusinessException(BusinessException e) {
        log.error("业务异常：", e); // 打印异常堆栈，便于排查
        return Result.error(e.getErrorEnum());
    }

    // ====================== 捕获参数校验异常（@Valid 注解触发） ======================
    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException e) {
        log.error("参数校验异常：", e);
        // 获取第一个校验错误信息
        String errorMsg = e.getBindingResult().getFieldError().getDefaultMessage();
        // 返回参数无效错误
        return Result.error(ErrorEnum.PARAM_INVALID, errorMsg);
    }

    // ====================== 捕获所有未处理的系统异常（兜底处理） ======================
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("系统内部异常：", e); // 打印完整异常堆栈，便于排查系统问题
        // 返回系统内部异常（不暴露具体异常信息给前端，避免安全风险）
        return Result.error(ErrorEnum.SYSTEM_ERROR);
    }
}