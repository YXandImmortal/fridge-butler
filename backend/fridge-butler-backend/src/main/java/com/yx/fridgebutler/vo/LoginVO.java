package com.yx.fridgebutler.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;

    private String username;

    private String mobile;

    private String roleName;

    private String createTime;

    private Long roleId;

    private Long userId;

    private Boolean rememberMe;

    private String avatar;

    private Long expireTime;
}
