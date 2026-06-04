package com.yx.fridgebutler.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 激活密钥列表响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivationKeyVO {

    /**
     * 密钥ID
     */
    private Long id;

    /**
     * 密钥字符串，如 FB-A3F9K2M1
     */
    private String keyCode;

    /**
     * 密钥状态：UNUSED-未使用, BOUND-已绑定, REVOKED-已收回, DESTROYED-已销毁
     */
    private String status;

    /**
     * 绑定用户ID
     */
    private Long boundUserId;

    /**
     * 绑定用户名
     */
    private String boundUsername;

    /**
     * 绑定时间
     */
    private String boundTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private String createTime;
}
