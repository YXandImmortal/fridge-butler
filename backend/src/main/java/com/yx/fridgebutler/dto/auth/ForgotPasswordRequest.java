package com.yx.fridgebutler.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 忘记密码请求 DTO。
 * <p>用于接收前端提交的邮箱地址，后端将向该邮箱发送密码重置验证码。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    /**
     * 用户邮箱，必填，需符合标准邮箱格式。
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}
