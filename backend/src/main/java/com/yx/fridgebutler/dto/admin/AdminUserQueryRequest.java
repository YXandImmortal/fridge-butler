package com.yx.fridgebutler.dto.admin;

import lombok.Data;

/**
 * 管理员用户列表查询请求 DTO
 */
@Data
public class AdminUserQueryRequest {

    /**
     * 关键词（用户名或手机号模糊匹配）
     */
    private String keyword;

    /**
     * 账号状态：true=禁用，false=正常，null=全部
     */
    private Boolean status;

    /**
     * 排序字段：createTime 或 lastLoginTime，默认 createTime
     */
    private String sortField;

    /**
     * 排序方向：asc 或 desc，默认 desc
     */
    private String sortOrder;

    /**
     * 页码，从 1 开始，默认 1
     */
    private Integer page = 1;

    /**
     * 每页条数，默认 10
     */
    private Integer size = 10;
}
