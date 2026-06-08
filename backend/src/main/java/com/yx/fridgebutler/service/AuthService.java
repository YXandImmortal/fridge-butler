package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.auth.EmailCaptchaRequest;
import com.yx.fridgebutler.dto.auth.ForgotPasswordRequest;
import com.yx.fridgebutler.dto.auth.LoginRequest;
import com.yx.fridgebutler.vo.LoginVO;
import com.yx.fridgebutler.dto.auth.RegisterRequest;
import com.yx.fridgebutler.dto.auth.ResetPasswordRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证服务接口。
 * <p>定义用户登录、注册等认证相关的业务逻辑。</p>
 */
public interface AuthService {

    /**
     * 用户登录。
     * <p>校验验证码、用户名/密码、账号状态，生成并返回 JWT Token。</p>
     *
     * @param request     登录请求参数
     * @param httpRequest HTTP 请求对象，用于获取客户端信息
     * @return 登录成功后的用户信息及 Token
     */
    LoginVO login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * 用户注册。
     * <p>校验验证码、密码一致性、用户名/手机号唯一性，创建新用户并直接颁发登录凭证。</p>
     *
     * @param request     注册请求参数
     * @param httpRequest HTTP 请求对象，用于获取客户端信息
     * @return 注册成功后的用户信息及 Token，与登录响应一致
     */
    LoginVO registerUser(RegisterRequest request, HttpServletRequest httpRequest);

    /**
     * 发送通用邮箱验证码。
     * <p>根据业务类型（REGISTER/RESET）发送邮箱验证码，支持注册验证和忘记密码。</p>
     *
     * @param request     邮箱验证码请求参数
     * @param httpRequest HTTP 请求对象，用于获取客户端信息
     */
    void sendEmailCaptcha(EmailCaptchaRequest request, HttpServletRequest httpRequest);

    /**
     * 发送密码重置验证码。
     * <p>校验邮箱是否已绑定用户，生成 6 位数字验证码并异步发送邮件。</p>
     *
     * @param request     忘记密码请求参数
     * @param httpRequest HTTP 请求对象，用于获取客户端信息
     */
    void sendPasswordResetCaptcha(ForgotPasswordRequest request, HttpServletRequest httpRequest);

    /**
     * 重置密码。
     * <p>校验邮箱验证码，校验两次新密码一致性，校验新密码不能与旧密码相同，更新密码。</p>
     *
     * @param request     重置密码请求参数
     * @param httpRequest HTTP 请求对象，用于获取客户端信息
     */
    void resetPassword(ResetPasswordRequest request, HttpServletRequest httpRequest);
}
