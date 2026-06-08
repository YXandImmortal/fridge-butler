package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.auth.EmailCaptchaRequest;
import com.yx.fridgebutler.dto.auth.ForgotPasswordRequest;
import com.yx.fridgebutler.dto.auth.LoginRequest;
import com.yx.fridgebutler.vo.LoginVO;
import com.yx.fridgebutler.dto.auth.RegisterRequest;
import com.yx.fridgebutler.dto.auth.ResetPasswordRequest;
import com.yx.fridgebutler.enums.ResultCode;
import com.yx.fridgebutler.service.AuthService;
import com.yx.fridgebutler.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * <p>
 * 处理用户登录、注册等认证相关请求。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     *
     * @param request    登录请求参数，包含账号、密码、验证码等信息
     * @param httpRequest HTTP请求对象，用于获取客户端IP地址
     * @return 登录成功返回包含登录信息的响应结果
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        log.info("用户登录请求，账号：{}，客户端IP：{}", request.getAccount(), getClientIp(httpRequest));
        LoginVO response = authService.login(request, httpRequest);
        log.info("用户登录成功，账号：{}", request.getAccount());
        return Result.success(ResultCode.LOGIN_SUCCESS, response);
    }

    /**
     * 普通用户注册
     * <p>
     * 注册成功后直接返回登录凭证，前端无需再次调用登录接口即可进入首页。
     * </p>
     *
     * @param request    注册请求参数，包含用户名、密码、手机号等信息
     * @param httpRequest HTTP请求对象，用于获取客户端IP地址
     * @return 注册成功返回与登录一致的响应结果，包含 JWT Token
     */
    @PostMapping("/register/user")
    public Result<LoginVO> registerUser(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        log.info("普通用户注册请求，用户名：{}，客户端IP：{}", request.getUsername(), getClientIp(httpRequest));
        LoginVO response = authService.registerUser(request, httpRequest);
        log.info("普通用户注册成功，用户名：{}", request.getUsername());
        return Result.success(ResultCode.REGISTER_SUCCESS, response);
    }

    /**
     * 发送通用邮箱验证码
     * <p>
     * 支持注册（REGISTER）和忘记密码（RESET）两种业务类型，
     * 向指定邮箱发送 6 位数字验证码。同一邮箱 60 秒内只能发送一次，验证码 5 分钟内有效。
     * </p>
     *
     * @param request    邮箱验证码请求参数，包含邮箱地址和业务类型
     * @param httpRequest HTTP请求对象，用于获取客户端IP地址
     * @return 发送成功返回提示信息
     */
    @PostMapping("/email/captcha")
    public Result<Void> sendEmailCaptcha(@Valid @RequestBody EmailCaptchaRequest request, HttpServletRequest httpRequest) {
        log.info("发送邮箱验证码请求，类型：{}，邮箱：{}，客户端IP：{}", request.getType(), request.getEmail(), getClientIp(httpRequest));
        authService.sendEmailCaptcha(request, httpRequest);
        return Result.success("验证码已发送，请查收邮件", null);
    }

    /**
     * 发送密码重置验证码
     * <p>
     * 向用户绑定的邮箱发送 6 位数字验证码，用于后续重置密码。
     * 同一邮箱 60 秒内只能发送一次，验证码 5 分钟内有效。
     * </p>
     *
     * @param request    忘记密码请求参数，包含邮箱地址
     * @param httpRequest HTTP请求对象，用于获取客户端IP地址
     * @return 发送成功返回提示信息
     */
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        log.info("发送密码重置验证码请求，邮箱：{}，客户端IP：{}", request.getEmail(), getClientIp(httpRequest));
        authService.sendPasswordResetCaptcha(request, httpRequest);
        return Result.success("密码重置验证码已发送，请查收邮件", null);
    }

    /**
     * 重置密码
     * <p>
     * 使用邮箱验证码校验用户身份，验证通过后更新用户密码。
     * </p>
     *
     * @param request    重置密码请求参数，包含邮箱、验证码、新密码和确认密码
     * @param httpRequest HTTP请求对象，用于获取客户端IP地址
     * @return 重置成功返回提示信息
     */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        log.info("重置密码请求，邮箱：{}，客户端IP：{}", request.getEmail(), getClientIp(httpRequest));
        authService.resetPassword(request, httpRequest);
        return Result.success("密码重置成功", null);
    }

    /**
     * 获取客户端真实 IP 地址
     * <p>
     * 依次从 X-Forwarded-For、Proxy-Client-IP、WL-Proxy-Client-IP 请求头中获取，
     * 若均无法获取则使用 RemoteAddr。
     * </p>
     *
     * @param request HTTP请求对象
     * @return 客户端的真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
