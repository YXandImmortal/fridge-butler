package com.yx.fridgebutler.dto.admin;

import lombok.Data;

/**
 * 管理员操作日志查询请求 DTO
 */
@Data
public class AdminLogQueryRequest {

    /**
     * 关键词（模糊匹配操作用户名或请求URI）
     */
    private String keyword;

    /**
     * 请求方法：GET/POST/PUT/DELETE/PATCH
     */
    private String method;

    /**
     * 响应状态码范围：200(2xx)/400(4xx)/500(5xx)
     */
    private Integer statusCode;

    /**
     * 开始日期（yyyy-MM-dd），包含当天
     */
    private String startDate;

    /**
     * 结束日期（yyyy-MM-dd），包含当天
     */
    private String endDate;

    /**
     * 页码，从 1 开始，默认 1
     */
    private Integer page = 1;

    /**
     * 每页条数，默认 20
     */
    private Integer size = 20;
}
