package com.yx.fridgebutler.job;

import com.yx.fridgebutler.service.DailyTipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每日小贴士生成定时任务。
 * <p>每天凌晨 1:00 自动生成当日小贴士，避免首次访问时等待 AI 响应。</p>
 */
@Slf4j
@Component
public class DailyTipGenerateJob {

    /** 每日小贴士服务层 */
    @Autowired
    private DailyTipService dailyTipService;

    /**
     * 每天凌晨 1:00 执行：预生成今日小贴士。
     * <p>如果已存在则跳过，保证高并发场景下的幂等性。</p>
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateDailyTip() {
        log.info("定时任务开始：预生成今日小贴士");
        boolean generated = dailyTipService.generateTodayTipIfAbsent();
        if (generated) {
            log.info("定时任务完成：成功生成今日小贴士");
        } else {
            log.info("定时任务完成：今日小贴士已存在，无需生成");
        }
    }
}
