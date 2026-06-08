package com.yx.fridgebutler.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用邮箱验证码发送请求 DTO。
 * <p>
 * 用于注册、忘记密码等匿名场景下，向指定邮箱发送验证码。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailCaptchaRequest {

    /**
     * 目标邮箱，必填，需符合标准邮箱格式。
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 业务类型，必填。
     * <p>可选值：REGISTER（注册）、RESET（忘记密码）。</p>
     */
    @NotBlank(message = "业务类型不能为空")
    @Pattern(regexp = "^(REGISTER|RESET)$", message = "业务类型只能是 REGISTER 或 RESET")
    private String type;
}
