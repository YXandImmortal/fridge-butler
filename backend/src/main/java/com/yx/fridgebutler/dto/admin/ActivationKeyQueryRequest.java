package com.yx.fridgebutler.dto.admin;

import lombok.Data;

/**
 * 激活密钥列表查询请求 DTO
 */
@Data
public class ActivationKeyQueryRequest {

    /**
     * 关键词（密钥码或备注模糊匹配）
     */
    private String keyword;

    /**
     * 状态筛选：UNUSED, ISSUED, BOUND, REVOKED, DESTROYED，null 表示全部
     */
    private String status;

    /**
     * 页码，从 1 开始，默认 1
     */
    private Integer page = 1;

    /**
     * 每页条数，默认 10
     */
    private Integer size = 10;
}
