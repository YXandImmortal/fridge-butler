package com.yx.fridgebutler.job;

import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.MonthlyReportService;
import com.yx.fridgebutler.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 月度报告生成定时任务。
 * <p>每月 1 号 09:00 执行，为所有用户生成上月报告并推送通知。</p>
 */
@Slf4j
@Component
public class MonthlyReportJob {

    /** 上海时区。 */
    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    /** 年月格式化器。 */
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    /** 分页大小。 */
    private static final int PAGE_SIZE = 100;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private MonthlyReportService monthlyReportService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 每月 1 号 09:00 执行。
     */
    @Scheduled(cron = "0 0 9 1 * ?")
    public void generateMonthlyReports() {
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
        // 上个月的年月
        LocalDate lastMonth = today.minusMonths(1);
        String yearMonth = lastMonth.format(YEAR_MONTH_FORMATTER);

        log.info("定时任务开始：月度报告生成，年月：{}", yearMonth);
        int page = 0;
        long totalProcessed = 0;

        while (true) {
            Page<SysUser> userPage = sysUserRepository.findAll(
                    PageRequest.of(page, PAGE_SIZE, Sort.by("id")));
            if (userPage.isEmpty()) {
                break;
            }

            for (SysUser user : userPage.getContent()) {
                if (Boolean.TRUE.equals(user.getIsDeleted())) {
                    continue;
                }
                try {
                    // 检查是否已生成
                    if (monthlyReportService.getReport(user.getId(), yearMonth) != null) {
                        continue;
                    }
                    monthlyReportService.generate(user.getId(), yearMonth);
                    totalProcessed++;

                    // 推送通知
                    notificationService.createSystemNotification(
                            user.getId(),
                            "📊 上月月度报告已生成",
                            "你的 " + yearMonth + " 月度冰箱管理报告已生成，快来看看你的保鲜成绩吧！",
                            "VIEW_MONTHLY_REPORT"
                    );
                } catch (Exception e) {
                    log.error("月度报告生成异常，用户ID：{}，年月：{}", user.getId(), yearMonth, e);
                }
            }

            if (!userPage.hasNext()) {
                break;
            }
            page++;
        }

        log.info("定时任务完成：月度报告生成，年月：{}，处理用户数量：{}", yearMonth, totalProcessed);
    }
}
