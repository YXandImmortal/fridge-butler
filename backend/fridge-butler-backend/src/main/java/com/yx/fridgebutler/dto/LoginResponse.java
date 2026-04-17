package com.yx.fridgebutler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private String username;

    private String roleName;

    private Long roleId;

    private Long userId;

    private Boolean rememberMe;

    private Long expireTime;
}
