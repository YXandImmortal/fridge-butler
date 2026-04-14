package com.yx.fridgebutler.exception;

import com.yx.fridgebutler.enums.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public static BusinessException authFailed(String message) {
        return new BusinessException(ResultCode.UNAUTHORIZED.getCode(), message);
    }

    public static BusinessException loginAuthFailed() {
        return new BusinessException(ResultCode.LOGIN_FAILED_USER_INFO_WRONG);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(ResultCode.FORBIDDEN.getCode(), message);
    }

    public static BusinessException loginForbidden() {
        return new BusinessException(ResultCode.LOGIN_FAILED_ACCOUNT_DISABLED);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(ResultCode.NOT_FOUND.getCode(), message);
    }

    public static BusinessException loginRoleNotFound() {
        return new BusinessException(ResultCode.LOGIN_FAILED_ROLE_NOT_FOUND);
    }
}
