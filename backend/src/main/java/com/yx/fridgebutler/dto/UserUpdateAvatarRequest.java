package com.yx.fridgebutler.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户更新头像请求 DTO。
 * <p>用于接收前端用户更新头像时提交的头像标识。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateAvatarRequest {

    /**
     * 头像标识，必填。
     */
    @NotBlank
    private String avatar;
}
