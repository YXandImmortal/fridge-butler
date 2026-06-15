package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.user.BindEmailRequest;
import com.yx.fridgebutler.dto.user.UserChangePasswordRequest;
import com.yx.fridgebutler.dto.user.UserEmailCaptchaRequest;
import com.yx.fridgebutler.dto.user.UserInitPasswordRequest;
import com.yx.fridgebutler.dto.user.UserUpdateAvatarRequest;
import com.yx.fridgebutler.dto.user.UserUpdateRequest;
import com.yx.fridgebutler.vo.gamification.ExpActionResultVO;
import com.yx.fridgebutler.vo.user.UserInfoVO;

/**
 * 用户服务接口。
 * <p>定义用户信息查询、更新、密码修改、头像更新等业务逻辑。</p>
 */
public interface UserService {

    /**
     * 获取当前登录用户的详细信息。
     *
     * @return 用户信息视图对象
     */
    UserInfoVO getUserInfo();

    /**
     * 更新当前登录用户的基本信息。
     *
     * @param request 用户信息更新请求参数
     */
    void updateUser(UserUpdateRequest request);

    /**
     * 修改当前登录用户的密码。
     *
     * @param request 密码修改请求参数
     */
    void changePassword(UserChangePasswordRequest request);

    /**
     * 更新当前登录用户的头像。
     *
     * @param request 头像更新请求参数
     */
    void updateAvatar(UserUpdateAvatarRequest request);

    /**
     * 标记当前登录用户的新手指引已完成。
     *
     * @return 操作结果，包含EXP信息
     */
    ExpActionResultVO completeGuide();

    /**
     * 首次登录初始化密码（无需原密码和验证码）。
     * <p>仅当用户 password_updated_at 为 null 时允许调用，用于首次登录或管理员重置密码后的强制改密。</p>
     *
     * @param request 初始化密码请求参数
     */
    void initPassword(UserInitPasswordRequest request);

    /**
     * 发送绑定/修改邮箱验证码。
     * <p>向目标邮箱发送 6 位数字验证码，用于登录后绑定或修改邮箱。</p>
     *
     * @param request 绑定邮箱验证码请求参数
     */
    void sendBindEmailCaptcha(UserEmailCaptchaRequest request);

    /**
     * 绑定或修改当前登录用户的邮箱。
     * <p>校验验证码及邮箱是否被其他用户占用，验证通过后更新邮箱。</p>
     *
     * @param request 绑定邮箱请求参数
     * @return 操作结果，包含EXP信息
     */
    ExpActionResultVO bindEmail(BindEmailRequest request);
}
