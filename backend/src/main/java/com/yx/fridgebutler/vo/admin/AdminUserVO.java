package com.yx.fridgebutler.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员用户管理列表/详情 VO
 * <p>展示用户信息（脱敏，不含密码）。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 头像标识
     */
    private String avatar;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 账号状态：true=正常，false=禁用
     */
    private Boolean status;

    /**
     * 是否已激活（密钥激活）
     */
    private Boolean isActivated;

    /**
     * 是否已完成新手引导
     */
    private Boolean guideCompleted;

    /**
     * 注册时间
     */
    private String createTime;

    /**
     * 最后登录时间
     */
    private String lastLoginTime;

    /**
     * 密码最后更新时间
     */
    private String passwordUpdatedAt;

    /**
     * 更新时间
     */
    private String updateTime;
}
