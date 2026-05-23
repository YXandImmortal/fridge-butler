package com.yx.fridgebutler.job;

import com.yx.fridgebutler.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 消息提醒生成定时任务。
 * <p>每天凌晨 02:00 扫描所有用户的冰箱物品，根据保质期自动生成临期/过期提醒。</p>
 */
@Slf4j
@Component
public class NotificationGenerateJob {

    @Autowired
    private NotificationService notificationService;

    /**
     * 每天凌晨 02:00 执行：生成临期和过期消息提醒。
     * <p>会自动去重，同一物品同一类型不会重复生成未读提醒。</p>
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateExpiringNotifications() {
        log.info("定时任务开始：生成临期/过期消息提醒");
        notificationService.generateExpiringNotifications();
        log.info("定时任务完成：生成临期/过期消息提醒");
    }
}
