package com.yx.fridgebutler.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息响应VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 是否已完成新手指引
     */
    private Boolean guideCompleted;

    /**
     * 是否已激活（true=已激活，false=未激活）
     */
    private Boolean isActivated;
}