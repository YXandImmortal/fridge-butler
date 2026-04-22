package com.yx.fridgebutler.enums;

import lombok.Getter;

@Getter
public enum ResultCode {
    /**
     * 通用状态码
     * <p>系统的其他模块或非特定场景使用</p>
     */
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    /**
     * 登录状态码
     * <p>登录模块使用</p>
     */
    LOGIN_SUCCESS(200, "登录成功"),
    LOGIN_FAILED_USER_INFO_WRONG(401, "用户名或密码错误"),
    LOGIN_FAILED_ACCOUNT_DISABLED(403, "账号已被禁用"),
    LOGIN_FAILED_ROLE_NOT_FOUND(404, "角色不存在"),

    /**
     * 注册状态码
     * <p>注册模块使用</p>
     */
    REGISTER_SUCCESS(200, "注册成功"),
    REGISTER_FAILED_USER_EXIST(400, "用户名已存在"),
    REGISTER_FAILED_PASSWORD_NOT_MATCH(400, "两次密码不一致"),
    REGISTER_FAILED_ROLE_NOT_FOUND(404, "注册角色不存在"),
    REGISTER_FAILED_PHONE_FORMAT_ERROR(400, "手机号格式不正确"),
    REGISTER_FAILED_PHONE_EXIST(400, "手机号已被注册"),

    /**
     * 验证码状态码
     * <p>验证码模块使用</p>
     */
    CAPTCHA_ERROR(400, "验证码错误"),

    /**
     * 修改密码状态码
     * <p>修改密码模块使用</p>
     */
    CHANGE_PASSWORD_FAILED_NOT_MATCH(400, "两次密码不一致"),
    CHANGE_PASSWORD_FAILED_ORIGINAL_WRONG(400, "原密码错误"),

    /**
     * 冰箱管理状态码
     * <p>冰箱管理模块使用</p>
     */
    CREATE_FRIDGE_FAILED_FRIDGE_EXISTS(400, "冰箱已经存在"),
    SORT_FAILED_UNKNOW_SORT_FIELD(400, "未知排序字段");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}