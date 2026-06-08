package com.yx.fridgebutler.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重置密码请求 DTO。
 * <p>用于接收前端提交的邮箱、验证码和新密码信息，完成密码重置操作。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    /**
     * 用户邮箱，必填。
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 邮箱验证码，必填，6 位数字。
     */
    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须为 6 位")
    private String captcha;

    /**
     * 新密码，必填。
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    /**
     * 确认新密码，必填，需与新密码一致。
     */
    @NotBlank(message = "确认新密码不能为空")
    private String confirmPassword;
}
