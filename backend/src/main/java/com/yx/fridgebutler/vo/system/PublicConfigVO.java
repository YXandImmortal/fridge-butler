package com.yx.fridgebutler.vo.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公开系统配置 VO
 * <p>供所有用户（含未登录）访问的动态配置项。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicConfigVO {

    /**
     * 系统公告内容
     */
    private String announcement;

    /**
     * 系统简介（关于页面展示）
     */
    private String systemDescription;

    /**
     * 管理员联系邮箱（用于用户联系管理员）
     */
    private String adminEmail;
}
