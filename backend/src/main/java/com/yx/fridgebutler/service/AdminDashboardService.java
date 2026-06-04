package com.yx.fridgebutler.service;

import com.yx.fridgebutler.vo.admin.AdminDashboardStatsVO;
import com.yx.fridgebutler.vo.admin.AdminTrendVO;

import java.util.List;

/**
 * 管理员数据看板服务接口
 */
public interface AdminDashboardService {

    /**
     * 获取数据看板核心统计数据
     *
     * @return 统计数据 VO
     */
    AdminDashboardStatsVO getStats();

    /**
     * 获取用户趋势数据（近 N 天）
     *
     * @param days 天数，默认 7
     * @return 每日趋势列表
     */
    List<AdminTrendVO> getTrend(Integer days);
}
