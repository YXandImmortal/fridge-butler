package com.yx.fridgebutler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Size(max = 50, message = "用户名长度不能超过50")
    private String username;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    @NotBlank(message = "密码不能为空")
    private String password;

    public void validateLoginParam() {
        if ((username == null || username.isBlank()) && (mobile == null || mobile.isBlank())) {
            throw new IllegalArgumentException("用户名或手机号不能为空");
        }
    }
}
