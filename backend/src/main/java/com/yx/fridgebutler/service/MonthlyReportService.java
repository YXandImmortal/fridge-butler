package com.yx.fridgebutler.service;

import com.yx.fridgebutler.vo.gamification.MonthlyReportRewardVO;
import com.yx.fridgebutler.vo.gamification.MonthlyReportVO;

/**
 * 月度报告服务接口。
 * <p>定义月度报告生成、查询、AI估算浪费金额等逻辑。</p>
 */
public interface MonthlyReportService {

    /**
     * 生成指定用户的月度报告。
     * <p>聚合当月数据，调用 DeepSeek AI 估算浪费金额，计算环保价值，保存报告记录。</p>
     *
     * @param userId    用户ID
     * @param yearMonth 年月，如 2026-05
     * @return 生成的月度报告 VO
     */
    MonthlyReportVO generate(Long userId, String yearMonth);

    /**
     * 获取指定用户的月度报告。
     * <p>如果报告不存在，返回 null（不自动生成）。</p>
     *
     * @param userId    用户ID
     * @param yearMonth 年月，如 2026-05
     * @return 月度报告 VO，不存在时返回 null
     */
    MonthlyReportVO getReport(Long userId, String yearMonth);

    /**
     * 标记月度报告为已查看，并发放首次查看 EXP。
     *
     * @param userId    用户ID
     * @param yearMonth 年月
     * @return 本次查看奖励信息（非首次查看 EXP 为 0）
     */
    MonthlyReportRewardVO markAsViewed(Long userId, String yearMonth);
}
