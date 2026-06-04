package com.yx.fridgebutler.exception;

import com.yx.fridgebutler.enums.ResultCode;
import com.yx.fridgebutler.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @ResponseStatus(HttpStatus.BAD_REQUEST)
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
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message;

        if (e.getBindingResult().hasFieldErrors("account")) {
            message = Objects.requireNonNull(e.getBindingResult().getFieldError("account")).getDefaultMessage();
        } else if (e.getBindingResult().hasFieldErrors("password")) {
            message = Objects.requireNonNull(e.getBindingResult().getFieldError("password")).getDefaultMessage();
        } else {
            FieldError fieldError = e.getBindingResult().getFieldError();
            message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        }

        log.warn("参数校验异常：{}", message);
        return Result.error(400, message);
    }

    /**
     * 处理权限不足异常（Controller/AOP 层抛出）
     * <p>
     * 捕获 AccessDeniedException，返回 403 统一响应。
     * 注意：Spring Security Filter 层抛出的权限异常需在 SecurityConfig 中配置。
     * </p>
     *
     * @param e 权限不足异常
     * @return 403 错误响应
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDenied(AccessDeniedException e) {
        log.warn("权限不足：{}", e.getMessage());
        return Result.error(ResultCode.FORBIDDEN.getCode(), "权限不足，无法访问该资源");
    }

    /**
     * 处理请求体参数校验异常（@RequestBody + @Valid）
     * <p>
     * 当接口入参使用 @RequestBody 结合 @Valid 校验失败时触发。
     * 收集所有字段错误，拼接为逗号分隔的字符串返回。
     * </p>
     *
     * @param e 参数校验异常
     * @return 400 错误响应，包含具体字段校验失败信息
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常：{}", msg);
        return Result.error(400, msg);
    }

    /**
     * 处理请求方法不支持异常
     * <p>
     * 当客户端使用了服务端不支持的 HTTP 方法（如 POST 接口用了 GET）时触发。
     * </p>
     *
     * @param e HTTP 方法不支持异常
     * @return 405 错误响应
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持：{}", e.getMessage());
        return Result.error(ResultCode.METHOD_NOT_ALLOWED.getCode(), "请求方法不支持: " + e.getMethod());
    }

    /**
     * 处理非法参数异常
     *
     * @param e 非法参数异常
     * @return 400 错误响应
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("非法参数：{}", e.getMessage());
        return Result.error(400, "请求参数非法：" + e.getMessage());
    }

    /**
     * 处理日期格式解析异常
     *
     * @param e 日期解析异常
     * @return 400 错误响应
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DateTimeParseException.class)
    public Result<Void> handleDateTimeParse(DateTimeParseException e) {
        log.warn("日期格式错误：{}", e.getMessage());
        return Result.error(400, "日期格式错误，请检查输入格式");
    }

    /**
     * 处理数据库数据冲突异常
     *
     * @param e 数据完整性冲突异常
     * @return 409 错误响应
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("数据冲突：{}", e.getMessage());
        return Result.error(409, "数据冲突，请检查是否存在重复或关联数据");
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
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "服务器内部错误，请稍后重试");
    }
}
