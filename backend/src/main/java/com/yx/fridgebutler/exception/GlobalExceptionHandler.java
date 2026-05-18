package com.yx.fridgebutler.exception;

import com.yx.fridgebutler.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理业务异常
     * <p>
     * 当系统抛出BusinessException时，该方法会捕获并处理异常，
     * 记录警告日志并返回统一的错误响应结果。
     * </p>
     *
     * @param e 业务异常对象，包含错误码和错误信息
     * @return 统一的错误响应结果，包含异常中的错误码和错误消息
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数绑定异常
     * <p>
     * 当系统抛出BindException时，该方法会捕获并处理异常，
     * 按照优先级顺序提取参数校验失败的错误信息：
     * 优先返回account字段的错误信息，其次返回password字段的错误信息，
     * 最后返回其他字段的错误信息。
     * 记录警告日志并返回400错误响应。
     * </p>
     *
     * @param e 绑定异常对象，包含参数校验的错误信息
     * @return 统一的错误响应结果，包含400状态码和参数校验失败的错误消息
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message;

        if (e.getBindingResult().hasFieldErrors("account")) {
            message = Objects.requireNonNull(e.getBindingResult().getFieldError("account")).getDefaultMessage();
        } else if (e.getBindingResult().hasFieldErrors("password")) {
            message = Objects.requireNonNull(e.getBindingResult().getFieldError("password")).getDefaultMessage();
        } else {
            message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        }

        log.warn("参数校验异常：{}", message);
        return Result.error(400, message);
    }

    /**
     * 处理系统异常
     * <p>
     * 当系统抛出未捕获的Exception时，该方法会作为全局异常处理器捕获异常，
     * 记录错误日志并返回500服务器内部错误的统一响应结果。
     * </p>
     *
     * @param e 异常对象，包含异常的详细信息
     * @return 统一的错误响应结果，包含500状态码和通用的服务器错误消息
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "服务器内部错误，请稍后重试");
    }
}
