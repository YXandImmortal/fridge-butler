package com.yx.fridgebutler.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordRequest {

    @NotBlank
    private String originalPassword;

    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmNewPassword;

    @NotBlank(message = "验证码不能为空")
    private String captcha;

    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;
}
