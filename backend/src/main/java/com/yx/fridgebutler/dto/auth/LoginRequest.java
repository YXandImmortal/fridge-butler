package com.yx.fridgebutler.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录请求 DTO。
 * <p>用于接收前端用户登录时提交的账号、密码和验证码信息。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * 登录账号（用户名或手机号），必填，最大长度50。
     */
    @NotBlank(message = "用户名或手机号不能为空")
    @Size(max = 50, message = "长度不能超过50")
    private String account;

    /**
     * 登录密码，必填。
     */
    @NotBlank(message = "密码不能为空")
    private String password;

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

    /**
     * 是否记住我，可选，默认 false。
     */
    private Boolean rememberMe;

}
