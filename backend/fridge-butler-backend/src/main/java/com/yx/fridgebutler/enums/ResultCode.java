package com.yx.fridgebutler.enums;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    LOGIN_SUCCESS(200, "登录成功"),
    LOGIN_FAILED_USER_INFO_WRONG(401, "用户名或密码错误"),
    LOGIN_FAILED_ACCOUNT_DISABLED(403, "账号已被禁用"),
    LOGIN_FAILED_ROLE_NOT_FOUND(404, "角色不存在");

    public static ResultCode getByCode(Integer code) {
        for (ResultCode rc : values()) {
            if (rc.getCode().equals(code)) {
                return rc;
            }
        }
        throw new IllegalArgumentException("无效的状态码：" + code);
    }

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
