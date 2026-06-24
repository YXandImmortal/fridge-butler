package com.yx.fridgebutler.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

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
    SUCCESS(200, HttpStatus.OK, "操作成功"),
    ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "系统错误"),
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "未授权"),
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "禁止访问"),
    NOT_FOUND(404, HttpStatus.NOT_FOUND, "资源不存在"),
    METHOD_NOT_ALLOWED(405, HttpStatus.METHOD_NOT_ALLOWED, "请求方法不支持"),

    /**
     * 登录状态码
     * <p>登录模块使用</p>
     */
    LOGIN_SUCCESS(200, HttpStatus.OK, "登录成功"),
    LOGIN_FAILED_USER_INFO_WRONG(401, HttpStatus.UNAUTHORIZED, "用户名或密码错误"),
    LOGIN_FAILED_ACCOUNT_DISABLED(403, HttpStatus.FORBIDDEN, "账号已被禁用"),
    USER_ACCOUNT_DISABLED(461, HttpStatus.FORBIDDEN, "账号已被禁用"),
    LOGIN_FAILED_ROLE_NOT_FOUND(404, HttpStatus.NOT_FOUND, "角色不存在"),

    /**
     * 注册状态码
     * <p>注册模块使用</p>
     */
    REGISTER_SUCCESS(200, HttpStatus.OK, "注册成功"),
    REGISTER_FAILED_USER_EXIST(400, HttpStatus.BAD_REQUEST, "用户名已存在"),
    REGISTER_FAILED_PASSWORD_NOT_MATCH(400, HttpStatus.BAD_REQUEST, "两次密码不一致"),
    REGISTER_FAILED_ROLE_NOT_FOUND(404, HttpStatus.NOT_FOUND, "注册角色不存在"),
    REGISTER_FAILED_PHONE_FORMAT_ERROR(400, HttpStatus.BAD_REQUEST, "手机号格式不正确"),
    REGISTER_FAILED_PHONE_EXIST(400, HttpStatus.BAD_REQUEST, "手机号已被注册"),

    /**
     * 验证码状态码
     * <p>验证码模块使用</p>
     */
    CAPTCHA_ERROR(400, HttpStatus.BAD_REQUEST, "验证码错误"),

    /**
     * 修改密码状态码
     * <p>修改密码模块使用</p>
     */
    CHANGE_PASSWORD_FAILED_NOT_MATCH(400, HttpStatus.BAD_REQUEST, "两次密码不一致"),
    CHANGE_PASSWORD_FAILED_ORIGINAL_WRONG(400, HttpStatus.BAD_REQUEST, "原密码错误"),
    INIT_PASSWORD_NOT_ALLOWED(400, HttpStatus.BAD_REQUEST, "您已设置过密码，无法再次初始化"),

    /**
     * 用户状态码
     * <p>用户模块使用</p>
     */
    USER_NOT_FOUND(404, HttpStatus.NOT_FOUND, "用户不存在"),
    ROLE_NOT_FOUND(404, HttpStatus.NOT_FOUND, "角色不存在"),
    UPDATE_USER_FAILED_USERNAME_EXIST(400, HttpStatus.BAD_REQUEST, "用户名已被占用"),
    UPDATE_USER_FAILED_PHONE_EXIST(400, HttpStatus.BAD_REQUEST, "手机号已被占用"),

    /**
     * 冰箱管理状态码
     * <p>冰箱管理模块使用</p>
     */
    FRIDGE_NOT_FOUND(404, HttpStatus.NOT_FOUND, "冰箱不存在"),
    CREATE_FRIDGE_FAILED_FRIDGE_EXISTS(400, HttpStatus.BAD_REQUEST, "冰箱已经存在"),
    UPDATE_FRIDGE_FAILED_NAME_EXISTS(400, HttpStatus.BAD_REQUEST, "冰箱名称已存在"),
    SORT_FAILED_UNKNOW_SORT_FIELD(400, HttpStatus.BAD_REQUEST, "未知排序字段"),

    /**
     * 物品管理状态码
     * <p>物品管理模块使用</p>
     */
    ITEM_NOT_FOUND(404, HttpStatus.NOT_FOUND, "物品不存在"),
    CATEGORY_NOT_FOUND(404, HttpStatus.NOT_FOUND, "物品分类不存在"),
    UNIT_NOT_FOUND(404, HttpStatus.NOT_FOUND, "物品单位不存在"),
    TAKE_OUT_NUM_EXCEED(400, HttpStatus.BAD_REQUEST, "取出数量不能大于现有数量"),
    CATEGORY_NAME_EXISTS(400, HttpStatus.BAD_REQUEST, "分类名称已存在"),
    CATEGORY_NOT_EDITABLE(403, HttpStatus.FORBIDDEN, "系统默认分类不允许编辑"),
    CATEGORY_IN_USE(400, HttpStatus.BAD_REQUEST, "该分类下存在物品，无法删除"),
    UNIT_TYPE_NOT_FOUND(404, HttpStatus.NOT_FOUND, "单位类型不存在"),
    UNIT_NAME_EXISTS(400, HttpStatus.BAD_REQUEST, "单位名称已存在"),
    UNIT_TYPE_NAME_EXISTS(400, HttpStatus.BAD_REQUEST, "单位类型名称已存在"),
    UNIT_NOT_EDITABLE(403, HttpStatus.FORBIDDEN, "系统默认单位不允许编辑"),
    UNIT_TYPE_NOT_EDITABLE(403, HttpStatus.FORBIDDEN, "系统默认单位类型不允许编辑"),

    /**
     * AI 服务状态码
     * <p>DeepSeek AI 大模型调用相关</p>
     */
    DEEPSEEK_API_ERROR(503, HttpStatus.SERVICE_UNAVAILABLE, "AI 服务调用失败"),

    /**
     * 采购助手状态码
     * <p>采购计划、模板、AI 推荐相关</p>
     */
    PURCHASE_PLAN_NOT_FOUND(404, HttpStatus.NOT_FOUND, "采购方案不存在"),
    PURCHASE_PLAN_CANNOT_UPDATE(400, HttpStatus.BAD_REQUEST, "只有待采购状态的计划可以修改"),
    PURCHASE_PLAN_CANNOT_DELETE(400, HttpStatus.BAD_REQUEST, "只有待采购或已取消状态的计划可以删除"),
    PURCHASE_PLAN_CANNOT_CANCEL(400, HttpStatus.BAD_REQUEST, "只有待采购状态的计划可以取消"),
    PURCHASE_PLAN_CANNOT_SETTLE(400, HttpStatus.BAD_REQUEST, "只有待采购状态的计划可以入库结算"),
    PURCHASE_PLAN_CANNOT_SEND_EMAIL(400, HttpStatus.BAD_REQUEST, "只有待采购状态的计划可以发送邮件"),
    PURCHASE_PLAN_ITEM_NOT_FOUND(404, HttpStatus.NOT_FOUND, "采购方案物品不存在"),
    PURCHASE_PLAN_ITEM_NOT_COVERED(400, HttpStatus.BAD_REQUEST, "请完成所有物品的核对"),
    PURCHASE_PLAN_TEMPLATE_NOT_FOUND(404, HttpStatus.NOT_FOUND, "采购计划模板不存在"),
    PURCHASE_PLAN_TEMPLATE_LIMIT_REACHED(400, HttpStatus.BAD_REQUEST, "用户采购计划模板数量已达上限（10个）"),
    PURCHASE_PLAN_TEMPLATE_NAME_EXISTS(400, HttpStatus.BAD_REQUEST, "模板名称已存在"),
    PURCHASE_PLAN_EMPTY_ITEMS(400, HttpStatus.BAD_REQUEST, "物品清单不能为空"),

    /**
     * 消息通知状态码
     * <p>消息提醒模块使用</p>
     */
    NOTIFICATION_NOT_FOUND(404, HttpStatus.NOT_FOUND, "消息不存在"),

    /**
     * 管理员模块状态码
     * <p>管理员后台功能使用</p>
     */
    ADMIN_CANNOT_DISABLE_SELF(403, HttpStatus.FORBIDDEN, "不能禁用或删除当前登录账号"),
    ADMIN_RESET_PASSWORD_FAILED(500, HttpStatus.INTERNAL_SERVER_ERROR, "重置密码失败"),

    /**
     * 邮件相关状态码
     * <p>邮件发送、验证码、密码重置模块使用</p>
     */
    EMAIL_NOT_BOUND(404, HttpStatus.NOT_FOUND, "该邮箱未绑定任何账号"),
    EMAIL_SEND_TOO_FREQUENT(429, HttpStatus.TOO_MANY_REQUESTS, "发送过于频繁，请稍后再试"),
    EMAIL_CAPTCHA_ERROR(400, HttpStatus.BAD_REQUEST, "验证码错误或已过期"),
    EMAIL_SEND_FAILED(400, HttpStatus.BAD_REQUEST, "邮件发送失败，请检查邮箱地址是否正确或稍后重试"),
    EMAIL_SERVICE_UNAVAILABLE(503, HttpStatus.SERVICE_UNAVAILABLE, "邮件服务暂不可用，请稍后重试"),
    EMAIL_FORMAT_ERROR(400, HttpStatus.BAD_REQUEST, "邮箱格式不正确"),
    EMAIL_ALREADY_BOUND(400, HttpStatus.BAD_REQUEST, "该邮箱已被其他账号绑定"),
    RESET_PASSWORD_FAILED_SAME_AS_OLD(400, HttpStatus.BAD_REQUEST, "新密码不能与旧密码相同"),

    /**
     * 激活密钥状态码
     * <p>激活密钥管理使用</p>
     */
    ACTIVATION_KEY_INVALID(400, HttpStatus.BAD_REQUEST, "激活密钥无效或不存在"),
    ACTIVATION_KEY_ALREADY_USED(400, HttpStatus.BAD_REQUEST, "激活密钥已被使用"),
    ACTIVATION_KEY_REVOKED(400, HttpStatus.BAD_REQUEST, "激活密钥已被收回"),
    ACTIVATION_KEY_DESTROYED(400, HttpStatus.BAD_REQUEST, "激活密钥已销毁"),
    USER_NOT_ACTIVATED(460, HttpStatus.BAD_REQUEST, "账号未激活，请先输入有效密钥"),

    /**
     * 重要通知广播状态码
     * <p>广播幂等校验失败</p>
     */
    DUPLICATE_BROADCAST(400, HttpStatus.BAD_REQUEST, "相同标题的重要通知在5分钟内已广播过"),

    /**
     * 重要通知模板不存在
     */
    IMPORTANT_NOTICE_NOT_FOUND(404, HttpStatus.NOT_FOUND, "重要通知模板不存在"),

    /**
     * 重要通知已关闭，无法广播
     */
    IMPORTANT_NOTICE_ALREADY_CLOSED(400, HttpStatus.BAD_REQUEST, "该重要通知已关闭，无法广播"),

    /**
     * 重要通知广播过于频繁
     */
    IMPORTANT_NOTICE_BROADCAST_TOO_FREQUENT(429, HttpStatus.TOO_MANY_REQUESTS, "该通知广播过于频繁，请稍后再试");

    /**
     * 状态码数值
     */
    private final Integer code;

    /**
     * HTTP 状态码
     */
    private final HttpStatus httpStatus;

    /**
     * 状态码描述信息
     */
    private final String message;

    /**
     * 构造结果状态码枚举
     *
     * @param code       状态码数值
     * @param httpStatus HTTP 状态码
     * @param message    状态码描述信息
     */
    ResultCode(Integer code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

}
