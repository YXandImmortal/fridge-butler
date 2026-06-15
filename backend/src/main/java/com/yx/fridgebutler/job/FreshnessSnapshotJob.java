package com.yx.fridgebutler.job;

import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.FreshnessScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 每日保鲜评分快照定时任务。
 * <p>每日 23:59:30 执行，遍历所有用户计算并保存当日保鲜评分快照。</p>
 */
@Slf4j
@Component
public class FreshnessSnapshotJob {

    /** 分页大小。 */
    private static final int PAGE_SIZE = 100;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private FreshnessScoreService freshnessScoreService;

    /**
     * 每日 23:59:30 执行，错峰于冰鲜判定任务。
     */
    @Scheduled(cron = "30 59 23 * * ?")
    public void dailySnapshot() {
        log.info("定时任务开始：每日保鲜评分快照生成");
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
                    freshnessScoreService.calculate(user.getId());
                    totalProcessed++;
                } catch (Exception e) {
                    log.error("保鲜评分快照生成异常，用户ID：{}", user.getId(), e);
                }
            }

            if (!userPage.hasNext()) {
                break;
            }
            page++;
        }

        log.info("定时任务完成：每日保鲜评分快照生成，处理用户数量：{}", totalProcessed);
    }
}
