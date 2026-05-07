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

    // ======================== 通用异常 ========================

    public static BusinessException authFailed() {
        return new BusinessException(ResultCode.UNAUTHORIZED);
    }

    public static BusinessException forbidden() {
        return new BusinessException(ResultCode.FORBIDDEN);
    }

    public static BusinessException notFound() {
        return new BusinessException(ResultCode.NOT_FOUND);
    }

    // ======================== 登录相关 ========================

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

    // ======================== 注册相关 ========================

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

    // ======================== 修改密码相关 ========================

    public static BusinessException changePasswordNotMatch() {
        return new BusinessException(ResultCode.CHANGE_PASSWORD_FAILED_NOT_MATCH);
    }

    public static BusinessException changePasswordOriginalWrong() {
        return new BusinessException(ResultCode.CHANGE_PASSWORD_FAILED_ORIGINAL_WRONG);
    }

    // ======================== 用户相关 ========================

    public static BusinessException userNotFound() {
        return new BusinessException(ResultCode.USER_NOT_FOUND);
    }

    public static BusinessException roleNotFound() {
        return new BusinessException(ResultCode.ROLE_NOT_FOUND);
    }

    public static BusinessException updateUserUsernameExist() {
        return new BusinessException(ResultCode.UPDATE_USER_FAILED_USERNAME_EXIST);
    }

    public static BusinessException updateUserPhoneExist() {
        return new BusinessException(ResultCode.UPDATE_USER_FAILED_PHONE_EXIST);
    }

    // ======================== 冰箱相关 ========================

    public static BusinessException fridgeNotFound() {
        return new BusinessException(ResultCode.FRIDGE_NOT_FOUND);
    }

    public static BusinessException fridgeNameExists() {
        return new BusinessException(ResultCode.CREATE_FRIDGE_FAILED_FRIDGE_EXISTS);
    }

    public static BusinessException updateFridgeNameExists() {
        return new BusinessException(ResultCode.UPDATE_FRIDGE_FAILED_NAME_EXISTS);
    }

    public static BusinessException unknownSortField() {
        return new BusinessException(ResultCode.SORT_FAILED_UNKNOW_SORT_FIELD);
    }

    // ======================== 物品相关 ========================

    public static BusinessException itemNotFound() {
        return new BusinessException(ResultCode.ITEM_NOT_FOUND);
    }

    public static BusinessException categoryNotFound() {
        return new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
    }

    public static BusinessException unitNotFound() {
        return new BusinessException(ResultCode.UNIT_NOT_FOUND);
    }

    public static BusinessException takeOutNumExceed() {
        return new BusinessException(ResultCode.TAKE_OUT_NUM_EXCEED);
    }

}
