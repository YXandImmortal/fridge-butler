package com.yx.fridgebutler.config;

import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.enums.BadgeCode;
import com.yx.fridgebutler.repository.BizFridgeRepository;
import com.yx.fridgebutler.repository.BizItemAddRecordRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.BadgeService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * 历史徽章补发启动初始化器。
 * <p>
 * 应用启动时，若开启 {@code system.badge.retrofit.enabled=true}，
 * 会分页扫描所有未删除用户，根据已有业务数据补发高可靠度徽章。
 * </p>
 * <p>幂等执行：已解锁的徽章不会重复发放。</p>
 */
@Component
@ConditionalOnProperty(
        prefix = "system.badge.retrofit",
        name = "enabled",
        havingValue = "true"
)
@Order(10)
@Slf4j
public class BadgeRetrofitInitializer implements ApplicationRunner {

    /** 上海时区。 */
    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");

    /** 每页处理用户数。 */
    private static final int PAGE_SIZE = 100;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private BizItemAddRecordRepository addRecordRepository;

    @Autowired
    private BadgeService badgeService;

    /**
     * 执行历史徽章补发。
     *
     * @param args 应用启动参数
     */
    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        log.info("========== 历史徽章补发开始 ==========");

        int page = 0;
        int processed = 0;
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
                    retrofitUser(user);
                    processed++;
                } catch (Exception e) {
                    log.error("历史徽章补发异常，用户ID：{}，用户名：{}", user.getId(), user.getUsername(), e);
                }
            }

            if (!userPage.hasNext()) {
                break;
            }
            page++;
        }

        log.info("========== 历史徽章补发结束，处理用户数：{} ==========", processed);
    }

    /**
     * 为单个用户补发应解锁的历史徽章。
     *
     * @param user 用户实体
     */
    private void retrofitUser(SysUser user) {
        Long userId = user.getId();
        boolean any = false;

        // 1. 绑定邮箱 → 安全达人
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            badgeService.unlockBadge(userId, BadgeCode.SECURITY_EXPERT, true);
            any = true;
        }

        // 2. 完成新手指引 → 指引完成者
        if (Boolean.TRUE.equals(user.getGuideCompleted())) {
            badgeService.unlockBadge(userId, BadgeCode.GUIDE_COMPLETE, true);
            any = true;
        }

        // 3. 食材添加数量 → 初出茅庐 / 钻石管家
        long addCount = addRecordRepository.countByOperatorId(userId);
        if (addCount >= 1) {
            badgeService.unlockBadge(userId, BadgeCode.FIRST_ITEM, true);
            any = true;
        }
        if (addCount >= 500) {
            badgeService.unlockBadge(userId, BadgeCode.DIAMOND_BUTLER, true);
        }

        // 4. 冰箱数量 → 冰箱达人
        long fridgeCount = fridgeRepository.countByOwnerIdAndIsDeletedFalse(userId);
        if (fridgeCount >= 3) {
            badgeService.unlockBadge(userId, BadgeCode.FRIDGE_MASTER, true);
            any = true;
        }

        // 5. 注册满 1 年 → 周年用户
        if (user.getCreateTime() != null) {
            LocalDate createDate = user.getCreateTime().atZone(ZONE_ID_SHANGHAI).toLocalDate();
            LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
            if (ChronoUnit.DAYS.between(createDate, today) >= 365) {
                badgeService.unlockBadge(userId, BadgeCode.ANNIVERSARY, true);
                any = true;
            }
        }

        if (any) {
            log.info("用户{}历史徽章补发完成", userId);
        }
    }
}
