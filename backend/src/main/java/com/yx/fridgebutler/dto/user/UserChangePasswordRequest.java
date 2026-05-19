package com.yx.fridgebutler.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户修改密码请求 DTO。
 * <p>用于接收前端用户修改密码时提交的原密码、新密码和验证码信息。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordRequest {

    /**
     * 原密码，必填。
     */
    @NotBlank
    private String originalPassword;

    /**
     * 新密码，必填。
     */
    @NotBlank
    private String newPassword;

    /**
     * 确认新密码，必填，需与新密码一致。
     */
    @NotBlank
    private String confirmNewPassword;

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
