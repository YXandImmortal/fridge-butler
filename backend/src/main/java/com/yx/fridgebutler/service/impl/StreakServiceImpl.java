package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.entity.BizFridge;
import com.yx.fridgebutler.entity.BizFridgeItem;
import com.yx.fridgebutler.entity.UserStreak;
import com.yx.fridgebutler.repository.BizFridgeItemRepository;
import com.yx.fridgebutler.repository.BizFridgeRepository;
import com.yx.fridgebutler.repository.UserStreakRepository;
import com.yx.fridgebutler.service.StreakService;
import com.yx.fridgebutler.vo.gamification.StreakInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 冰鲜连续天数服务实现类。
 * <p>处理冰鲜连续天数管理、保护机制、月度重置等逻辑。</p>
 */
@Slf4j
@Service
public class StreakServiceImpl implements StreakService {

    /** 上海时区。 */
    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    /** 月份格式化器，格式为 yyyy-MM。 */
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Autowired
    private UserStreakRepository userStreakRepository;

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private BizFridgeItemRepository itemRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserStreak getOrCreateUserStreak(Long userId) {
        return userStreakRepository.findByUserId(userId).orElseGet(() -> {
            UserStreak streak = new UserStreak();
            streak.setUserId(userId);
            streak.setCurrentStreak(0);
            streak.setMaxStreak(0);
            streak.setProtectCountRemaining(2);
            streak.setProtectCountTotal(2);
            streak.setProtectCountUsed(0);
            streak.setProtectResetMonth(LocalDate.now(ZONE_ID_SHANGHAI).format(MONTH_FORMATTER));
            streak.setAutoProtectEnabled((byte) 1);
            streak.setProtectNotifyEnabled((byte) 1);
            streak.setLastCheckResult((byte) 0);
            streak.setCreatedAt(Instant.now());
            streak.setUpdatedAt(Instant.now());
            return userStreakRepository.save(streak);
        });
    }

    /**
     * {@inheritDoc}
     * <p>
     * 重置规则：若当前保护次数 &lt; 2，重置到 2；若 ≥ 2，保留现有次数。
     * </p>
     */
    @Override
    @Transactional
    public void resetMonthlyProtectIfNeeded(UserStreak streak) {
        String currentMonth = LocalDate.now(ZONE_ID_SHANGHAI).format(MONTH_FORMATTER);
        if (currentMonth.equals(streak.getProtectResetMonth())) {
            return; // 本月已重置过
        }

        int newTotal = streak.getProtectCountRemaining() >= 2 ? streak.getProtectCountTotal() : 2;
        int newRemaining = streak.getProtectCountRemaining() >= 2 ? streak.getProtectCountRemaining() : 2;

        streak.setProtectCountTotal(newTotal);
        streak.setProtectCountRemaining(newRemaining);
        streak.setProtectCountUsed(0);
        streak.setProtectResetMonth(currentMonth);
        streak.setUpdatedAt(Instant.now());

        userStreakRepository.save(streak);
        log.info("用户{}保护次数月度重置，本月总次数：{}，剩余：{}",
                streak.getUserId(), newTotal, newRemaining);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean useProtect(Long userId) {
        UserStreak streak = getOrCreateUserStreak(userId);
        if (streak.getProtectCountRemaining() <= 0) {
            log.warn("用户{}保护次数不足，无法使用保护", userId);
            return false;
        }
        streak.setProtectCountRemaining(streak.getProtectCountRemaining() - 1);
        streak.setProtectCountUsed(streak.getProtectCountUsed() + 1);
        streak.setUpdatedAt(Instant.now());
        userStreakRepository.save(streak);
        log.info("用户{}使用 1 次保护，剩余{}次", userId, streak.getProtectCountRemaining());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void incrementStreak(Long userId) {
        UserStreak streak = getOrCreateUserStreak(userId);
        streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        if (streak.getCurrentStreak() > streak.getMaxStreak()) {
            streak.setMaxStreak(streak.getCurrentStreak());
        }
        streak.setLastCheckResult((byte) 1);
        streak.setLastCheckDate(LocalDate.now(ZONE_ID_SHANGHAI));
        streak.setUpdatedAt(Instant.now());
        userStreakRepository.save(streak);
        log.info("用户{}冰鲜连续天数增加至{}天", userId, streak.getCurrentStreak());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void breakStreak(Long userId) {
        UserStreak streak = getOrCreateUserStreak(userId);
        streak.setCurrentStreak(0);
        streak.setLastCheckResult((byte) 0);
        streak.setLastCheckDate(LocalDate.now(ZONE_ID_SHANGHAI));
        streak.setUpdatedAt(Instant.now());
        userStreakRepository.save(streak);
        log.info("用户{}冰鲜连续天数已中断", userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreakInfoVO getStreakInfo(Long userId) {
        UserStreak streak = getOrCreateUserStreak(userId);
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);

        // 冰鲜融化预警：当日有过期物品且未处理
        boolean meltWarning;
        if (today.equals(streak.getLastCheckDate())) {
            // 今天已经判定过，直接按判定结果
            meltWarning = streak.getLastCheckResult() != null && streak.getLastCheckResult() == 0;
        } else {
            // 今天尚未判定，实时查询是否有过期物品
            meltWarning = checkTodayHasExpiredItems(userId);
        }

        return StreakInfoVO.builder()
                .currentStreak(streak.getCurrentStreak())
                .maxStreak(streak.getMaxStreak())
                .protectRemaining(streak.getProtectCountRemaining())
                .protectTotal(streak.getProtectCountTotal())
                .protectAutoEnabled(streak.getAutoProtectEnabled() != null && streak.getAutoProtectEnabled() == 1)
                .meltWarning(meltWarning)
                .build();
    }

    /**
     * 检查用户当日是否有过期物品。
     *
     * @param userId 用户ID
     * @return true 表示当日有过期物品
     */
    private boolean checkTodayHasExpiredItems(Long userId) {
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
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
}
