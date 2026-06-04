package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.service.AdminDashboardService;
import com.yx.fridgebutler.vo.Result;
import com.yx.fridgebutler.vo.admin.AdminDashboardStatsVO;
import com.yx.fridgebutler.vo.admin.AdminTrendVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员数据看板控制器
 * <p>
 * 提供系统运营数据统计接口，仅管理员角色可访问。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    /**
     * 获取数据看板核心统计数据
     *
     * @return 用户总数、今日新增、冰箱总数、物品总数、今日活跃
     */
    @GetMapping("/stats")
    public Result<AdminDashboardStatsVO> getStats() {
        log.debug("管理员请求数据看板统计");
        return Result.success(adminDashboardService.getStats());
    }

    /**
     * 获取用户趋势数据
     *
     * @param days 查询天数，默认 7 天，最大 90 天
     * @return 每日新增用户与活跃用户趋势列表
     */
    @GetMapping("/trend")
    public Result<List<AdminTrendVO>> getTrend(@RequestParam(required = false) Integer days) {
        log.debug("管理员请求数据看板趋势，天数：{}", days);
        return Result.success(adminDashboardService.getTrend(days));
    }
}
