package com.yx.fridgebutler.job;

import com.yx.fridgebutler.entity.BizFridge;
import com.yx.fridgebutler.entity.BizFridgeItem;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.entity.UserStreak;
import com.yx.fridgebutler.enums.ExpActionType;
import com.yx.fridgebutler.repository.BizFridgeItemRepository;
import com.yx.fridgebutler.repository.BizFridgeRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.enums.BadgeTriggerType;
import com.yx.fridgebutler.service.BadgeService;
import com.yx.fridgebutler.service.ExpService;
import com.yx.fridgebutler.service.NotificationService;
import com.yx.fridgebutler.service.StreakService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * 冰鲜连续天数每日判定定时任务。
 * <p>每日 23:59 执行，遍历所有用户：
 * <ol>
 *   <li>检查并重置月度保护次数</li>
 *   <li>判定用户当日是否有过期物品</li>
 *   <li>有过期：尝试自动保护；保护失败则中断连续天数</li>
 *   <li>无过期：连续天数 +1，发放 NO_EXPIRE EXP 和 STREAK_BONUS EXP</li>
 * </ol>
 * </p>
 */
@Slf4j
@Component
public class StreakDailyCheckJob {

    /** 上海时区。 */
    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    /** 分页大小。 */
    private static final int PAGE_SIZE = 100;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private BizFridgeItemRepository itemRepository;

    @Autowired
    private StreakService streakService;

    @Autowired
    private ExpService expService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private com.yx.fridgebutler.repository.UserStreakRepository userStreakRepository;

    @Autowired
    private BadgeService badgeService;

    /**
     * 每日 23:59 执行冰鲜判定。
     */
    @Scheduled(cron = "0 59 23 * * ?", zone = "Asia/Shanghai")
    public void dailyStreakCheck() {
        log.info("定时任务开始：每日冰鲜连续天数判定");
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
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
                    processUserStreak(user.getId(), today);
                    totalProcessed++;
                } catch (Exception e) {
                    log.error("冰鲜判定处理用户异常，用户ID：{}", user.getId(), e);
                }
            }

            if (!userPage.hasNext()) {
                break;
            }
            page++;
        }

        log.info("定时任务完成：每日冰鲜连续天数判定，处理用户数量：{}", totalProcessed);
    }

    /**
     * 处理单个用户的冰鲜判定逻辑。
     *
     * @param userId 用户ID
     * @param today  当前日期
     */
    private void processUserStreak(Long userId, LocalDate today) {
        // 1. 获取或创建 streak 记录，并检查月度保护重置
        UserStreak streak = streakService.getOrCreateUserStreak(userId);

        // 防止重复判定
        if (today.equals(streak.getLastCheckDate())) {
            log.debug("用户{}今日已判定过，跳过", userId);
            return;
        }

        streakService.resetMonthlyProtectIfNeeded(streak);

        // 2. 判定当日是否有过期物品
        boolean hasExpired = checkUserHasExpiredItems(userId, today);

        if (hasExpired) {
            // 3. 有过期：尝试自动保护
            boolean autoProtect = streak.getAutoProtectEnabled() != null && streak.getAutoProtectEnabled() == 1;
            if (autoProtect && streak.getProtectCountRemaining() > 0) {
                // 使用保护，连续天数不中断
                streakService.useProtect(userId);
                streak = streakService.getOrCreateUserStreak(userId); // 重新获取最新状态
                log.info("用户{}冰鲜保护已使用，连续天数保持{}天，剩余保护{}次",
                        userId, streak.getCurrentStreak(), streak.getProtectCountRemaining());

                // 发送保护使用通知
                if (streak.getProtectNotifyEnabled() != null && streak.getProtectNotifyEnabled() == 1) {
                    notificationService.createSystemNotification(
                            userId,
                            "🧊 冰鲜保护已使用",
                            "你的冰鲜差点融化了！已使用 1 次保护次数重新冻住，本月剩余 "
                                    + streak.getProtectCountRemaining() + " 次。记得及时清理冰箱哦~",
                            "NONE"
                    );
                }
            } else {
                // 保护失败，中断连续天数
                int oldStreak = streak.getCurrentStreak(); // 保存旧连续天数用于通知
                streakService.breakStreak(userId);
                streak = streakService.getOrCreateUserStreak(userId); // 重新获取最新状态
                log.info("用户{}冰鲜已融化，连续天数归零", userId);

                // 发送融化通知
                notificationService.createSystemNotification(
                        userId,
                        "🧊 冰鲜已融化",
                        "冰鲜已融化，连续 " + oldStreak + " 天纪录中断。明天开始新的冰鲜之旅吧！",
                        "NONE"
                );
            }
        } else {
            // 4. 无过期：连续天数 +1
            streakService.incrementStreak(userId);
            streak = streakService.getOrCreateUserStreak(userId);
            int currentStreak = streak.getCurrentStreak();
            log.info("用户{}当日无过期物品，冰鲜连续天数增加至{}天", userId, currentStreak);

            // 5. 发放今日无过期 EXP
            expService.addExp(userId, ExpActionType.NO_EXPIRE);

            // 6. 计算并发放连续天数加成 EXP（绕过每日上限，鼓励用户保持连续）
            int bonus = calculateStreakBonus(currentStreak);
            if (bonus > 0) {
                expService.addExpBypassDailyCap(userId, ExpActionType.STREAK_BONUS, bonus, null,
                        "连续冰鲜 " + currentStreak + " 天加成");
            }

            // 7. 徽章触发：冰鲜连续天数检查
            badgeService.checkAndUnlock(userId, BadgeTriggerType.STREAK_CHECK, currentStreak);
        }

        // 更新最后判定日期
        streak.setLastCheckDate(today);
        userStreakRepository.save(streak);
    }

    /**
     * 检查用户当日是否有过期物品。
     *
     * @param userId 用户ID
     * @param today  当前日期
     * @return true 表示有过期物品
     */
    private boolean checkUserHasExpiredItems(Long userId, LocalDate today) {
        List<BizFridge> fridges = fridgeRepository.findByOwnerIdAndIsDeletedFalse(userId, Sort.unsorted());
        for (BizFridge fridge : fridges) {
            List<BizFridgeItem> items = itemRepository.findByFridgeIdAndIsDeletedFalse(fridge.getId());
            for (BizFridgeItem item : items) {
                if (isItemExpired(item, today)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断物品是否已过期。
     *
     * @param item  物品实体
     * @param today 当前日期
     * @return true 表示已过期
     */
    private boolean isItemExpired(BizFridgeItem item, LocalDate today) {
        if (item.getProductionDate() == null || item.getShelfLifeDays() == null) {
            return false;
        }
        LocalDate expireDate = item.getProductionDate().plusDays(item.getShelfLifeDays());
        return expireDate.isBefore(today);
    }

    /**
     * 计算连续天数加成 EXP。
     * <p>每日加成 = 当前连续天数 × 3，上不封顶，鼓励用户保持连续。</p>
     *
     * @param streak 当前连续天数
     * @return 加成 EXP
     */
    private int calculateStreakBonus(int streak) {
        return streak * 3;
    }
}
