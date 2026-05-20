package com.yx.fridgebutler.service;

import com.yx.fridgebutler.vo.dailytip.DailyTipVO;

import java.time.LocalDate;

/**
 * 每日小贴士服务接口。
 */
public interface DailyTipService {

    /**
     * 获取今日小贴士。
     * <p>如果数据库中不存在，则实时调用 AI 生成并保存。</p>
     *
     * @return 今日小贴士 VO
     */
    DailyTipVO getTodayTip();

    /**
     * 获取指定日期的小贴士。
     *
     * @param date 日期
     * @return 小贴士 VO，不存在返回 null
     */
    DailyTipVO getTipByDate(LocalDate date);

    /**
     * 强制生成今日小贴士（用于定时任务）。
     * <p>如果今日已存在，则跳过不覆盖。</p>
     *
     * @return 是否成功生成
     */
    boolean generateTodayTipIfAbsent();
}
