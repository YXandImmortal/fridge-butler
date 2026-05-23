package com.yx.fridgebutler.enums;

import lombok.Getter;

/**
 * 结果状态码枚举
 * <p>
 * 定义系统中所有接口返回的状态码和对应的消息描述。
 * 按功能模块分组，包括通用、登录、注册、验证码、用户、冰箱、物品等模块。
 * </p>
 */
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
     * 用户状态码
     * <p>用户模块使用</p>
     */
    USER_NOT_FOUND(404, "用户不存在"),
    ROLE_NOT_FOUND(404, "角色不存在"),
    UPDATE_USER_FAILED_USERNAME_EXIST(400, "用户名已被占用"),
    UPDATE_USER_FAILED_PHONE_EXIST(400, "手机号已被占用"),

    /**
     * 冰箱管理状态码
     * <p>冰箱管理模块使用</p>
     */
    FRIDGE_NOT_FOUND(404, "冰箱不存在"),
    CREATE_FRIDGE_FAILED_FRIDGE_EXISTS(400, "冰箱已经存在"),
    UPDATE_FRIDGE_FAILED_NAME_EXISTS(400, "冰箱名称已存在"),
    SORT_FAILED_UNKNOW_SORT_FIELD(400, "未知排序字段"),

    /**
     * 物品管理状态码
     * <p>物品管理模块使用</p>
     */
    ITEM_NOT_FOUND(404, "物品不存在"),
    CATEGORY_NOT_FOUND(404, "物品分类不存在"),
    UNIT_NOT_FOUND(404, "物品单位不存在"),
    TAKE_OUT_NUM_EXCEED(400, "取出数量不能大于现有数量"),
    CATEGORY_NAME_EXISTS(400, "分类名称已存在"),
    CATEGORY_NOT_EDITABLE(403, "系统默认分类不允许编辑"),
    CATEGORY_IN_USE(400, "该分类下存在物品，无法删除"),
    UNIT_TYPE_NOT_FOUND(404, "单位类型不存在"),
    UNIT_NAME_EXISTS(400, "单位名称已存在"),
    UNIT_TYPE_NAME_EXISTS(400, "单位类型名称已存在"),
    UNIT_NOT_EDITABLE(403, "系统默认单位不允许编辑"),
    UNIT_TYPE_NOT_EDITABLE(403, "系统默认单位类型不允许编辑"),

    /**
     * AI 服务状态码
     * <p>DeepSeek AI 大模型调用相关</p>
     */
    DEEPSEEK_API_ERROR(503, "AI 服务调用失败"),

    /**
     * 消息通知状态码
     * <p>消息提醒模块使用</p>
     */
    NOTIFICATION_NOT_FOUND(404, "消息不存在");

    /**
     * 状态码数值
     */
    private final Integer code;

    /**
     * 状态码描述信息
     */
    private final String message;

    /**
     * 构造结果状态码枚举
     *
     * @param code    状态码数值
     * @param message 状态码描述信息
     */
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
