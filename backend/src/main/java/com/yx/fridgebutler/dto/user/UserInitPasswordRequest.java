package com.yx.fridgebutler.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户首次登录初始化密码请求 DTO。
 * <p>用于用户首次登录（或管理员重置密码后首次登录）时设置新密码，无需原密码和验证码。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInitPasswordRequest {

    /**
     * 新密码，必填。
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    /**
     * 确认新密码，必填，需与新密码一致。
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmNewPassword;
}
