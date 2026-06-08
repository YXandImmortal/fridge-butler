package com.yx.fridgebutler.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录用户绑定/修改邮箱请求 DTO。
 * <p>
 * 用于已登录用户确认绑定或修改邮箱地址。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BindEmailRequest {

    /**
     * 目标邮箱，必填。
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
}
