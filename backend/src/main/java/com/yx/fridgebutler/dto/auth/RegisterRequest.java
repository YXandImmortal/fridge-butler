package com.yx.fridgebutler.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册请求 DTO。
 * <p>用于接收前端用户注册时提交的用户名、密码、手机号和验证码信息。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * 用户名，必填，最大长度50。
     */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50")
    private String username;

    /**
     * 登录密码，必填。
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 确认密码，必填，需与密码一致。
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 手机号，可选，需符合中国大陆手机号格式。
     */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 邮箱，可选，需符合标准邮箱格式。
     * <p>填写后需要同时提供邮箱验证码。</p>
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 邮箱验证码，选填，当填写邮箱时必须提供。
     */
    @Pattern(regexp = "^$|^\\d{6}$", message = "邮箱验证码必须为 6 位数字")
    private String emailCaptcha;

    /**
     * 验证码，必填。
     */
    @NotBlank(message = "验证码不能为空")
    private String captcha;

    /**
     * 验证码ID，用于校验验证码，必填。
     */
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;
}
