package com.yx.fridgebutler.vo;

import com.yx.fridgebutler.enums.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用响应结果VO
 *
 * @param <T> 响应数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 返回成功结果
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    /**
     * 返回成功结果（自定义消息）
     *
     * @param message 自定义成功消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 返回成功结果（自定义结果码）
     *
     * @param resultCode 结果码枚举
     * @param data       响应数据
     * @param <T>        数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(ResultCode resultCode, T data) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .data(data)
                .build();
    }

    /**
     * 返回错误结果（自定义错误码和消息）
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 错误响应结果
     */
    public static Result<Void> error(Integer code, String message) {
        return Result.<Void>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }

    /**
     * 返回错误结果（默认错误码）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误响应结果
     */
    public static <T> Result<T> error(String message) {
        return Result.<T>builder()
                .code(ResultCode.ERROR.getCode())
                .message(message)
                .data(null)
                .build();
    }

    /**
     * 返回错误结果（指定结果码枚举）
     *
     * @param resultCode 结果码枚举
     * @param <T>        数据类型
     * @return 错误响应结果
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .data(null)
                .build();
    }
}