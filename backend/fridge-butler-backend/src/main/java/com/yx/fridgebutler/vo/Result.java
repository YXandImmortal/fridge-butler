package com.yx.fridgebutler.vo;

import com.yx.fridgebutler.enums.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Integer code;

    private String message;

    private T data;

    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .build();
    }

    public static <T> Result<T> success(ResultCode resultCode, T data) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .data(data)
                .build();
    }


    public static Result<Void> error(Integer code, String message) {
        return Result.<Void>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> Result<T> error(String message) {
        return Result.<T>builder()
                .code(ResultCode.ERROR.getCode())
                .message(message)
                .data(null)
                .build();
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return Result.<T>builder()
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .data(null)
                .build();
    }
}
