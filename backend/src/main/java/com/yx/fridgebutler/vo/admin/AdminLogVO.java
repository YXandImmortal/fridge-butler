package com.yx.fridgebutler.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员操作日志 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogVO {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求URI
     */
    private String uri;

    /**
     * 客户端IP
     */
    private String ip;

    /**
     * 请求参数（脱敏后）
     */
    private String params;

    /**
     * 响应状态码
     */
    private Integer statusCode;

    /**
     * 耗时（毫秒）
     */
    private Integer durationMs;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 创建时间（yyyy-MM-dd HH:mm:ss）
     */
    private String createTime;
}
