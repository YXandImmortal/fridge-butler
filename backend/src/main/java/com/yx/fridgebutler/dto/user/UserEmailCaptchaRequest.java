package com.yx.fridgebutler.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录用户发送绑定邮箱验证码请求 DTO。
 * <p>
 * 用于已登录用户向目标邮箱发送绑定/修改邮箱的验证码。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEmailCaptchaRequest {

    /**
     * 目标邮箱，必填，需符合标准邮箱格式。
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}
