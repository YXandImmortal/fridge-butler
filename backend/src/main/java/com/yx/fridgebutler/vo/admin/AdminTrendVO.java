package com.yx.fridgebutler.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员数据看板趋势 VO
 * <p>展示指定日期范围内的用户注册与活跃趋势。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTrendVO {

    /**
     * 日期，格式 yyyy-MM-dd
     */
    private String date;

    /**
     * 当日新增用户数
     */
    private Long newUsers;

    /**
     * 当日活跃用户数
     */
    private Long activeUsers;
}
