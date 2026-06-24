package com.yx.fridgebutler.exception;

import com.yx.fridgebutler.enums.ResultCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常
 * <p>
 * 系统业务逻辑层面的自定义运行时异常，用于封装业务错误码和错误信息。
 * 提供了一系列静态工厂方法，方便快速创建各类常见的业务异常实例。
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 业务错误码
     */
    private final Integer code;

    /**
     * HTTP 状态码
     */
    private final HttpStatus httpStatus;

    /**
     * 构造业务异常
     *
     * @param code       错误码
     * @param httpStatus HTTP 状态码
     * @param message    错误信息
     */
    public BusinessException(Integer code, HttpStatus httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    /**
     * 根据结果状态码构造业务异常
     *
     * @param resultCode 结果状态码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.httpStatus = resultCode.getHttpStatus();
    }



    // ======================== 通用异常 ========================

    /**
     * 创建认证失败异常
     *
     * @return 未授权业务异常
     */
    public static BusinessException authFailed() {
        return new BusinessException(ResultCode.UNAUTHORIZED);
    }

    /**
     * 创建禁止访问异常
     *
     * @return 禁止访问业务异常
     */
    public static BusinessException forbidden() {
        return new BusinessException(ResultCode.FORBIDDEN);
    }

    /**
     * 创建资源不存在异常
     *
     * @return 资源不存在业务异常
     */
    public static BusinessException notFound() {
        return new BusinessException(ResultCode.NOT_FOUND);
    }

    // ======================== 登录相关 ========================

    /**
     * 创建登录认证失败异常（用户名或密码错误）
     *
     * @return 登录失败业务异常
     */
    public static BusinessException loginAuthFailed() {
        return new BusinessException(ResultCode.LOGIN_FAILED_USER_INFO_WRONG);
    }

    /**
     * 创建登录禁用异常（账号被禁用）
     *
     * @return 账号禁用业务异常
     */
    public static BusinessException loginForbidden() {
        return new BusinessException(ResultCode.USER_ACCOUNT_DISABLED);
    }

    /**
     * 创建登录角色不存在异常
     *
     * @return 角色不存在业务异常
     */
    public static BusinessException loginRoleNotFound() {
        return new BusinessException(ResultCode.LOGIN_FAILED_ROLE_NOT_FOUND);
    }

    /**
     * 创建验证码错误异常
     *
     * @return 验证码错误业务异常
     */
    public static BusinessException loginCaptchaError() {
        return new BusinessException(ResultCode.CAPTCHA_ERROR);
    }

    // ======================== 注册相关 ========================

    /**
     * 创建用户已存在异常
     *
     * @return 用户已存在业务异常
     */
    public static BusinessException registerUserExist() {
        return new BusinessException(ResultCode.REGISTER_FAILED_USER_EXIST);
    }

    /**
     * 创建注册密码不匹配异常
     *
     * @return 两次密码不一致业务异常
     */
    public static BusinessException registerPasswordNotMatch() {
        return new BusinessException(ResultCode.REGISTER_FAILED_PASSWORD_NOT_MATCH);
    }

    /**
     * 创建注册角色不存在异常
     *
     * @return 注册角色不存在业务异常
     */
    public static BusinessException registerUserRoleNotFound() {
        return new BusinessException(ResultCode.REGISTER_FAILED_ROLE_NOT_FOUND);
    }

    /**
     * 创建手机号格式错误异常
     *
     * @return 手机号格式错误业务异常
     */
    public static BusinessException registerPhoneFormatError() {
        return new BusinessException(ResultCode.REGISTER_FAILED_PHONE_FORMAT_ERROR);
    }

    /**
     * 创建手机号已注册异常
     *
     * @return 手机号已存在业务异常
     */
    public static BusinessException registerPhoneExist() {
        return new BusinessException(ResultCode.REGISTER_FAILED_PHONE_EXIST);
    }

    // ======================== 修改密码相关 ========================

    /**
     * 创建修改密码不匹配异常
     *
     * @return 两次密码不一致业务异常
     */
    public static BusinessException changePasswordNotMatch() {
        return new BusinessException(ResultCode.CHANGE_PASSWORD_FAILED_NOT_MATCH);
    }

    /**
     * 创建原密码错误异常
     *
     * @return 原密码错误业务异常
     */
    public static BusinessException changePasswordOriginalWrong() {
        return new BusinessException(ResultCode.CHANGE_PASSWORD_FAILED_ORIGINAL_WRONG);
    }

    /**
     * 创建初始化密码不允许异常（已设置过密码）
     *
     * @return 初始化密码不允许业务异常
     */
    public static BusinessException initPasswordNotAllowed() {
        return new BusinessException(ResultCode.INIT_PASSWORD_NOT_ALLOWED);
    }

    // ======================== 用户相关 ========================

    /**
     * 创建用户不存在异常
     *
     * @return 用户不存在业务异常
     */
    public static BusinessException userNotFound() {
        return new BusinessException(ResultCode.USER_NOT_FOUND);
    }

    /**
     * 创建角色不存在异常
     *
     * @return 角色不存在业务异常
     */
    public static BusinessException roleNotFound() {
        return new BusinessException(ResultCode.ROLE_NOT_FOUND);
    }

    /**
     * 创建用户名已存在异常（更新用户时）
     *
     * @return 用户名已被占用业务异常
     */
    public static BusinessException updateUserUsernameExist() {
        return new BusinessException(ResultCode.UPDATE_USER_FAILED_USERNAME_EXIST);
    }

    /**
     * 创建手机号已存在异常（更新用户时）
     *
     * @return 手机号已被占用业务异常
     */
    public static BusinessException updateUserPhoneExist() {
        return new BusinessException(ResultCode.UPDATE_USER_FAILED_PHONE_EXIST);
    }

    // ======================== 冰箱相关 ========================

    /**
     * 创建冰箱不存在异常
     *
     * @return 冰箱不存在业务异常
     */
    public static BusinessException fridgeNotFound() {
        return new BusinessException(ResultCode.FRIDGE_NOT_FOUND);
    }

    /**
     * 创建冰箱名称已存在异常
     *
     * @return 冰箱已存在业务异常
     */
    public static BusinessException fridgeNameExists() {
        return new BusinessException(ResultCode.CREATE_FRIDGE_FAILED_FRIDGE_EXISTS);
    }

    /**
     * 创建更新冰箱名称已存在异常
     *
     * @return 冰箱名称已存在业务异常
     */
    public static BusinessException updateFridgeNameExists() {
        return new BusinessException(ResultCode.UPDATE_FRIDGE_FAILED_NAME_EXISTS);
    }

    /**
     * 创建未知排序字段异常
     *
     * @return 未知排序字段业务异常
     */
    public static BusinessException unknownSortField() {
        return new BusinessException(ResultCode.SORT_FAILED_UNKNOW_SORT_FIELD);
    }

    // ======================== 物品相关 ========================

    /**
     * 创建物品不存在异常
     *
     * @return 物品不存在业务异常
     */
    public static BusinessException itemNotFound() {
        return new BusinessException(ResultCode.ITEM_NOT_FOUND);
    }

    /**
     * 创建物品分类不存在异常
     *
     * @return 物品分类不存在业务异常
     */
    public static BusinessException categoryNotFound() {
        return new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
    }

    /**
     * 创建物品单位不存在异常
     *
     * @return 物品单位不存在业务异常
     */
    public static BusinessException unitNotFound() {
        return new BusinessException(ResultCode.UNIT_NOT_FOUND);
    }

    /**
     * 创建取出数量超出异常
     *
     * @return 取出数量不能大于现有数量业务异常
     */
    public static BusinessException takeOutNumExceed() {
        return new BusinessException(ResultCode.TAKE_OUT_NUM_EXCEED);
    }

    /**
     * 创建分类名称已存在异常
     *
     * @return 分类名称已存在业务异常
     */
    public static BusinessException categoryNameExists() {
        return new BusinessException(ResultCode.CATEGORY_NAME_EXISTS);
    }

    /**
     * 创建分类不可编辑异常（系统默认分类）
     *
     * @return 系统默认分类不允许编辑业务异常
     */
    public static BusinessException categoryNotEditable() {
        return new BusinessException(ResultCode.CATEGORY_NOT_EDITABLE);
    }

    /**
     * 创建分类正在使用中异常
     *
     * @return 该分类下存在物品，无法删除业务异常
     */
    public static BusinessException categoryInUse() {
        return new BusinessException(ResultCode.CATEGORY_IN_USE);
    }

    /**
     * 创建单位类型不存在异常
     *
     * @return 单位类型不存在业务异常
     */
    public static BusinessException unitTypeNotFound() {
        return new BusinessException(ResultCode.UNIT_TYPE_NOT_FOUND);
    }

    /**
     * 创建单位名称已存在异常
     *
     * @return 单位名称已存在业务异常
     */
    public static BusinessException unitNameExists() {
        return new BusinessException(ResultCode.UNIT_NAME_EXISTS);
    }

    /**
     * 创建单位类型名称已存在异常
     *
     * @return 单位类型名称已存在业务异常
     */
    public static BusinessException unitTypeNameExists() {
        return new BusinessException(ResultCode.UNIT_TYPE_NAME_EXISTS);
    }

    /**
     * 创建单位不可编辑异常（系统默认单位）
     *
     * @return 系统默认单位不允许编辑业务异常
     */
    public static BusinessException unitNotEditable() {
        return new BusinessException(ResultCode.UNIT_NOT_EDITABLE);
    }

    /**
     * 创建单位类型不可编辑异常（系统默认单位类型）
     *
     * @return 系统默认单位类型不允许编辑业务异常
     */
    public static BusinessException unitTypeNotEditable() {
        return new BusinessException(ResultCode.UNIT_TYPE_NOT_EDITABLE);
    }

    // ======================== 消息通知相关 ========================

    /**
     * 创建消息不存在异常
     *
     * @return 消息不存在业务异常
     */
    public static BusinessException notificationNotFound() {
        return new BusinessException(ResultCode.NOTIFICATION_NOT_FOUND);
    }

    /**
     * 创建重复广播异常
     *
     * @return 5分钟内相同标题通知已存在业务异常
     */
    public static BusinessException duplicateBroadcast() {
        return new BusinessException(ResultCode.DUPLICATE_BROADCAST);
    }

    /**
     * 创建重要通知模板不存在异常
     *
     * @return 重要通知模板不存在业务异常
     */
    public static BusinessException importantNoticeNotFound() {
        return new BusinessException(ResultCode.IMPORTANT_NOTICE_NOT_FOUND);
    }

    /**
     * 创建重要通知已关闭异常
     *
     * @return 该重要通知已关闭业务异常
     */
    public static BusinessException importantNoticeAlreadyClosed() {
        return new BusinessException(ResultCode.IMPORTANT_NOTICE_ALREADY_CLOSED);
    }

    /**
     * 创建重要通知广播过于频繁异常
     *
     * @return 该通知广播过于频繁业务异常
     */
    public static BusinessException importantNoticeBroadcastTooFrequent() {
        return new BusinessException(ResultCode.IMPORTANT_NOTICE_BROADCAST_TOO_FREQUENT);
    }

    // ======================== 管理员相关 ========================

    /**
     * 创建不能禁用自己异常
     *
     * @return 不能禁用当前登录账号业务异常
     */
    public static BusinessException adminCannotDisableSelf() {
        return new BusinessException(ResultCode.ADMIN_CANNOT_DISABLE_SELF);
    }

    // ======================== 激活密钥相关 ========================

    /**
     * 创建激活密钥无效异常
     *
     * @return 激活密钥无效业务异常
     */
    public static BusinessException activationKeyInvalid() {
        return new BusinessException(ResultCode.ACTIVATION_KEY_INVALID);
    }

    /**
     * 创建激活密钥已被使用异常
     *
     * @return 激活密钥已被使用业务异常
     */
    public static BusinessException activationKeyAlreadyUsed() {
        return new BusinessException(ResultCode.ACTIVATION_KEY_ALREADY_USED);
    }

    /**
     * 创建激活密钥已被收回异常
     *
     * @return 激活密钥已被收回业务异常
     */
    public static BusinessException activationKeyRevoked() {
        return new BusinessException(ResultCode.ACTIVATION_KEY_REVOKED);
    }

    /**
     * 创建账号未激活异常
     *
     * @return 账号未激活业务异常
     */
    public static BusinessException userNotActivated() {
        return new BusinessException(ResultCode.USER_NOT_ACTIVATED);
    }

    // ======================== 邮件相关 ========================

    /**
     * 创建邮箱未绑定异常
     *
     * @return 邮箱未绑定业务异常
     */
    public static BusinessException emailNotBound() {
        return new BusinessException(ResultCode.EMAIL_NOT_BOUND);
    }

    /**
     * 创建邮件发送过于频繁异常
     *
     * @return 发送过于频繁业务异常
     */
    public static BusinessException emailSendTooFrequent() {
        return new BusinessException(ResultCode.EMAIL_SEND_TOO_FREQUENT);
    }

    /**
     * 创建邮件验证码错误异常
     *
     * @return 验证码错误或已过期业务异常
     */
    public static BusinessException emailCaptchaError() {
        return new BusinessException(ResultCode.EMAIL_CAPTCHA_ERROR);
    }

    /**
     * 创建邮件发送失败异常（收件人相关，如邮箱不存在、被SMTP拒收）。
     * <p>HTTP 状态码为 400，属于客户端可修正的错误。</p>
     *
     * @return 邮件发送失败业务异常
     */
    public static BusinessException emailSendFailed() {
        return new BusinessException(ResultCode.EMAIL_SEND_FAILED);
    }

    /**
     * 创建邮件服务不可用异常（SMTP连接失败、认证失败、服务未配置等）。
     * <p>HTTP 状态码为 503，属于服务端暂不可用。</p>
     *
     * @return 邮件服务不可用业务异常
     */
    public static BusinessException emailServiceUnavailable() {
        return new BusinessException(ResultCode.EMAIL_SERVICE_UNAVAILABLE);
    }

    /**
     * 创建邮箱格式错误异常
     *
     * @return 邮箱格式不正确业务异常
     */
    public static BusinessException emailFormatError() {
        return new BusinessException(ResultCode.EMAIL_FORMAT_ERROR);
    }

    /**
     * 创建邮箱已被绑定异常
     *
     * @return 邮箱已被其他账号绑定业务异常
     */
    public static BusinessException emailAlreadyBound() {
        return new BusinessException(ResultCode.EMAIL_ALREADY_BOUND);
    }

    /**
     * 创建新密码与旧密码相同异常
     *
     * @return 新密码不能与旧密码相同业务异常
     */
    public static BusinessException resetPasswordSameAsOld() {
        return new BusinessException(ResultCode.RESET_PASSWORD_FAILED_SAME_AS_OLD);
    }

    // ======================== AI 服务相关 ========================

    /**
     * 创建 DeepSeek API 调用失败异常
     *
     * @return AI 服务调用失败业务异常
     */
    public static BusinessException deepSeekApiError() {
        return new BusinessException(ResultCode.DEEPSEEK_API_ERROR);
    }

    /**
     * 创建 DeepSeek API 调用失败异常（携带详细错误信息）
     *
     * @param detail 详细错误信息
     * @return AI 服务调用失败业务异常
     */
    public static BusinessException deepSeekApiError(String detail) {
        return new BusinessException(
                ResultCode.DEEPSEEK_API_ERROR.getCode(),
                ResultCode.DEEPSEEK_API_ERROR.getHttpStatus(),
                ResultCode.DEEPSEEK_API_ERROR.getMessage() + ": " + detail
        );
    }

    // ======================== 采购助手相关 ========================

    public static BusinessException purchasePlanNotFound() {
        return new BusinessException(ResultCode.PURCHASE_PLAN_NOT_FOUND);
    }

    public static BusinessException purchasePlanCannotUpdate() {
        return new BusinessException(ResultCode.PURCHASE_PLAN_CANNOT_UPDATE);
    }

    public static BusinessException purchasePlanCannotDelete() {
        return new BusinessException(ResultCode.PURCHASE_PLAN_CANNOT_DELETE);
    }

    public static BusinessException purchasePlanCannotCancel() {
        return new BusinessException(ResultCode.PURCHASE_PLAN_CANNOT_CANCEL);
    }

    public static BusinessException purchasePlanCannotSettle() {
        return new BusinessException(ResultCode.PURCHASE_PLAN_CANNOT_SETTLE);
    }

    public static BusinessException purchasePlanCannotSendEmail() {
        return new BusinessException(ResultCode.PURCHASE_PLAN_CANNOT_SEND_EMAIL);
    }

    public static BusinessException purchasePlanItemNotFound() {
        return new BusinessException(ResultCode.PURCHASE_PLAN_ITEM_NOT_FOUND);
    }

    public static BusinessException purchasePlanItemNotCovered() {
        return new BusinessException(ResultCode.PURCHASE_PLAN_ITEM_NOT_COVERED);
    }

    public static BusinessException purchasePlanTemplateNotFound() {
        return new BusinessException(ResultCode.PURCHASE_PLAN_TEMPLATE_NOT_FOUND);
    }

    public static BusinessException purchasePlanTemplateLimitReached() {
        return new BusinessException(ResultCode.PURCHASE_PLAN_TEMPLATE_LIMIT_REACHED);
    }

    public static BusinessException purchasePlanTemplateNameExists() {
        return new BusinessException(ResultCode.PURCHASE_PLAN_TEMPLATE_NAME_EXISTS);
    }

    public static BusinessException purchasePlanEmptyItems() {
        return new BusinessException(ResultCode.PURCHASE_PLAN_EMPTY_ITEMS);
    }

}
