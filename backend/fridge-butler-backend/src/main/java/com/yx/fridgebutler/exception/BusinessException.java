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

    public static BusinessException authFailed() {
        return new BusinessException(ResultCode.UNAUTHORIZED);
    }

    public static BusinessException forbidden() {
        return new BusinessException(ResultCode.FORBIDDEN);
    }

    public static BusinessException notFound() {
        return new BusinessException(ResultCode.NOT_FOUND);
    }

    public static BusinessException loginAuthFailed() {
        return new BusinessException(ResultCode.LOGIN_FAILED_USER_INFO_WRONG);
    }

    public static BusinessException loginForbidden() {
        return new BusinessException(ResultCode.LOGIN_FAILED_ACCOUNT_DISABLED);
    }

    public static BusinessException loginRoleNotFound() {
        return new BusinessException(ResultCode.LOGIN_FAILED_ROLE_NOT_FOUND);
    }

    public static BusinessException loginCaptchaError() {
        return new BusinessException(ResultCode.CAPTCHA_ERROR);
    }

    public static BusinessException registerUserExist() {
        return new BusinessException(ResultCode.REGISTER_FAILED_USER_EXIST);
    }

    public static BusinessException registerPasswordNotMatch() {
        return new BusinessException(ResultCode.REGISTER_FAILED_PASSWORD_NOT_MATCH);
    }

    public static BusinessException registerUserRoleNotFound() {
        return new BusinessException(ResultCode.REGISTER_FAILED_ROLE_NOT_FOUND);
    }

    public static BusinessException registerPhoneFormatError() {
        return new BusinessException(ResultCode.REGISTER_FAILED_PHONE_FORMAT_ERROR);
    }

    public static BusinessException registerPhoneExist() {
        return new BusinessException(ResultCode.REGISTER_FAILED_PHONE_EXIST);
    }

    public static BusinessException changePasswordNotMatch() {
        return new BusinessException(ResultCode.CHANGE_PASSWORD_FAILD_NOT_MATCH);
    }
}