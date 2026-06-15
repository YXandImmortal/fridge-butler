package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.entity.*;
import com.yx.fridgebutler.enums.BadgeCode;
import com.yx.fridgebutler.enums.BadgeTriggerType;
import com.yx.fridgebutler.enums.ExpActionType;
import com.yx.fridgebutler.repository.*;
import com.yx.fridgebutler.service.BadgeService;
import com.yx.fridgebutler.service.ExpService;
import com.yx.fridgebutler.service.NotificationService;
import com.yx.fridgebutler.vo.gamification.BadgeUnlockInfo;
import com.yx.fridgebutler.vo.gamification.BadgeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 徽章服务实现类。
 * <p>处理徽章解锁判定、行为计数、用户徽章查询等核心逻辑。</p>
 */
@Slf4j
@Service
public class BadgeServiceImpl implements BadgeService {

    /** 上海时区。 */
    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");

    /** 计数器类型：夜猫子。 */
    private static final String COUNTER_NIGHT_OWL = "NIGHT_OWL";
    /** 计数器类型：早起鸟。 */
    private static final String COUNTER_EARLY_BIRD = "EARLY_BIRD";
    /** 计数器类型：数据控。 */
    private static final String COUNTER_DATA_CENTER = "DATA_CENTER_VIEW";
    /** 计数器类型：大厨认证。 */
    private static final String COUNTER_CHEF_COOK = "CHEF_COOK";
    /** 计数器类型：整理专家（按日）。 */
    private static final String COUNTER_ORGANIZE_DAY = "ORGANIZE_DAY";
    /** 计数器类型：AI好友。 */
    private static final String COUNTER_AI_FRIEND = "AI_FRIEND";
    /** 计数器类型：预言家。 */
    private static final String COUNTER_PROPHET = "PROPHET";

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private UserActionCounterRepository userActionCounterRepository;

    @Autowired
    private BizItemAddRecordRepository addRecordRepository;

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private UserStreakRepository userStreakRepository;

    @Autowired
    private ExpService expService;

    @Autowired
    private NotificationService notificationService;

    /** 当前检查过程中新解锁的徽章列表（ThreadLocal，避免多线程串号）。 */
    private final ThreadLocal<List<BadgeUnlockInfo>> currentUnlockResults = new ThreadLocal<>();

    // ======================== 核心入口 ========================

    /**
     * {@inheritDoc}
     * <p>
     * 根据触发类型更新计数器，然后检查相关徽章条件。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndUnlock(Long userId, BadgeTriggerType triggerType, Object context) {
        checkAndUnlockWithResult(userId, triggerType, context);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 根据触发类型更新计数器，然后检查相关徽章，并返回本次新解锁的徽章列表。
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BadgeUnlockInfo> checkAndUnlockWithResult(Long userId, BadgeTriggerType triggerType, Object context) {
        if (userId == null || triggerType == null) {
            return new ArrayList<>();
        }

        currentUnlockResults.set(new ArrayList<>());
        try {
            // 1. 更新计数器
            updateCounters(userId, triggerType, context);

            // 2. 检查徽章
            checkBadges(userId, triggerType, context);

            return currentUnlockResults.get();
        } catch (Exception e) {
            log.error("徽章检查异常，用户ID：{}，触发类型：{}", userId, triggerType, e);
            return currentUnlockResults.get();
        } finally {
            currentUnlockResults.remove();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BadgeVO> getUserBadges(Long userId) {
        // 获取用户已解锁的徽章
        List<UserBadge> unlockedBadges = userBadgeRepository.findByUserId(userId);
        Map<String, UserBadge> unlockedMap = new java.util.HashMap<>();
        for (UserBadge ub : unlockedBadges) {
            unlockedMap.put(ub.getBadgeCode(), ub);
        }

        // 构建全部徽章列表
        List<BadgeVO> result = new ArrayList<>();
        for (BadgeCode badgeCode : BadgeCode.values()) {
            UserBadge ub = unlockedMap.get(badgeCode.getCode());
            result.add(BadgeVO.builder()
                    .code(badgeCode.getCode())
                    .name(badgeCode.getName())
                    .iconClass(badgeCode.getIconClass())
                    .description(badgeCode.getDescription())
                    .unlocked(ub != null)
                    .unlockedAt(ub != null ? ub.getUnlockedAt() : null)
                    .expReward(badgeCode.getExpReward())
                    .unlockConditionDesc(badgeCode.getUnlockConditionDesc())
                    .build());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasBadge(Long userId, BadgeCode badgeCode) {
        return userBadgeRepository.existsByUserIdAndBadgeCode(userId, badgeCode.getCode());
    }

    /**
     * {@inheritDoc}
     * <p>
     * 解锁徽章流程：
     * <ol>
     *   <li>检查是否已解锁，已解锁则跳过</li>
     *   <li>保存 user_badge 记录</li>
     *   <li>发送系统通知</li>
     *   <li>发放 EXP 奖励</li>
     * </ol>
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockBadge(Long userId, BadgeCode badgeCode) {
        if (hasBadge(userId, badgeCode)) {
            return;
        }

        UserBadge userBadge = new UserBadge();
        userBadge.setUserId(userId);
        userBadge.setBadgeCode(badgeCode.getCode());
        userBadge.setUnlockedAt(Instant.now());
        userBadgeRepository.save(userBadge);

        // 发送系统通知
        notificationService.createSystemNotification(
                userId,
                "🎖️ 解锁新徽章：" + badgeCode.getName(),
                "恭喜解锁「" + badgeCode.getName() + "」徽章！" + badgeCode.getDescription() + "，获得 " + badgeCode.getExpReward() + " EXP 奖励。",
                "NONE"
        );

        // 发放 EXP
        expService.addExp(userId, ExpActionType.BADGE, badgeCode.getExpReward(), null,
                "解锁徽章：" + badgeCode.getName());

        // 若处于 checkAndUnlockWithResult 调用链中，收集解锁信息
        List<BadgeUnlockInfo> results = currentUnlockResults.get();
        if (results != null) {
            results.add(BadgeUnlockInfo.builder()
                    .code(badgeCode.getCode())
                    .name(badgeCode.getName())
                    .iconClass(badgeCode.getIconClass())
                    .description(badgeCode.getDescription())
                    .expReward(badgeCode.getExpReward())
                    .build());
        }

        log.info("用户{}解锁徽章：{}，获得{} EXP", userId, badgeCode.getName(), badgeCode.getExpReward());
    }

    /**
     * 手动解锁指定徽章，支持控制通知并绕过每日总 EXP 上限。
     * <p>用于历史徽章补发等场景，确保徽章经验值必须发放到位。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockBadge(Long userId, BadgeCode badgeCode, boolean sendNotification) {
        if (hasBadge(userId, badgeCode)) {
            return;
        }

        UserBadge userBadge = new UserBadge();
        userBadge.setUserId(userId);
        userBadge.setBadgeCode(badgeCode.getCode());
        userBadge.setUnlockedAt(Instant.now());
        userBadgeRepository.save(userBadge);

        if (sendNotification) {
            notificationService.createSystemNotification(
                    userId,
                    "🎖️ 解锁新徽章：" + badgeCode.getName(),
                    "恭喜解锁「" + badgeCode.getName() + "」徽章！" + badgeCode.getDescription() + "，获得 " + badgeCode.getExpReward() + " EXP 奖励。",
                    "NONE"
            );
        }

        // 历史补发场景：绕过每日总上限，确保徽章经验值发放到位
        expService.addExpBypassDailyCap(userId, ExpActionType.BADGE, badgeCode.getExpReward(), null,
                "解锁徽章：" + badgeCode.getName());

        // 若处于 checkAndUnlockWithResult 调用链中，收集解锁信息
        List<BadgeUnlockInfo> results = currentUnlockResults.get();
        if (results != null) {
            results.add(BadgeUnlockInfo.builder()
                    .code(badgeCode.getCode())
                    .name(badgeCode.getName())
                    .iconClass(badgeCode.getIconClass())
                    .description(badgeCode.getDescription())
                    .expReward(badgeCode.getExpReward())
                    .build());
        }

        log.info("用户{}解锁徽章：{}，获得{} EXP{}", userId, badgeCode.getName(), badgeCode.getExpReward(),
                sendNotification ? "" : "（静默）");
    }

    // ======================== 计数器更新 ========================

    /**
     * 根据触发类型更新相关计数器。
     */
    private void updateCounters(Long userId, BadgeTriggerType triggerType, Object context) {
        LocalTime now = LocalTime.now(ZONE_ID_SHANGHAI);

        switch (triggerType) {
            case ADD_ITEM, TAKE_OUT_ITEM, UPDATE_ITEM -> {
                // 时间类徽章计数
                checkAndIncrementTimeCounter(userId, now);
            }
            case AI_CHAT -> {
                incrementGlobalCounter(userId, COUNTER_AI_FRIEND);
                checkAndIncrementTimeCounter(userId, now);
            }
            case AI_RECIPE -> {
                incrementGlobalCounter(userId, COUNTER_CHEF_COOK);
            }
            case VIEW_DATA_CENTER -> {
                incrementGlobalCounter(userId, COUNTER_DATA_CENTER);
            }
            case LOGIN -> {
                checkAndIncrementTimeCounter(userId, now);
            }
            case TAKE_OUT_EXPIRING -> {
                // 临期取出，增加预言家计数
                incrementGlobalCounter(userId, COUNTER_PROPHET);
                checkAndIncrementTimeCounter(userId, now);
            }
            case ORGANIZE -> {
                // 单日整理≥10件，记录一次
                recordOrganizeDay(userId);
            }
            default -> {
                // 其他类型不需要计数
            }
        }
    }

    /**
     * 检查并增加时间类计数器（夜猫子、早起鸟）。
     */
    private void checkAndIncrementTimeCounter(Long userId, LocalTime time) {
        // 夜猫子：23:00 - 05:00
        if (time.isAfter(LocalTime.of(22, 59)) || time.isBefore(LocalTime.of(5, 0))) {
            incrementGlobalCounter(userId, COUNTER_NIGHT_OWL);
        }
        // 早起鸟：05:00 - 08:00
        if (!time.isBefore(LocalTime.of(5, 0)) && time.isBefore(LocalTime.of(8, 0))) {
            incrementGlobalCounter(userId, COUNTER_EARLY_BIRD);
        }
    }

    /**
     * 增加全局累计计数器（count_date 为 null）。
     */
    private void incrementGlobalCounter(Long userId, String counterType) {
        UserActionCounter counter = userActionCounterRepository
                .findByUserIdAndCounterTypeAndCountDateIsNull(userId, counterType)
                .orElse(null);

        if (counter == null) {
            counter = new UserActionCounter();
            counter.setUserId(userId);
            counter.setCounterType(counterType);
            counter.setCountValue(1);
            counter.setCountDate(null);
            counter.setUpdatedAt(Instant.now());
        } else {
            counter.setCountValue(counter.getCountValue() + 1);
            counter.setUpdatedAt(Instant.now());
        }
        userActionCounterRepository.save(counter);
    }

    /**
     * 记录单日整理达标（用于整理专家徽章）。
     */
    private void recordOrganizeDay(Long userId) {
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
        boolean exists = userActionCounterRepository
                .findByUserIdAndCounterTypeAndCountDate(userId, COUNTER_ORGANIZE_DAY, today)
                .isPresent();
        if (!exists) {
            UserActionCounter counter = new UserActionCounter();
            counter.setUserId(userId);
            counter.setCounterType(COUNTER_ORGANIZE_DAY);
            counter.setCountValue(1);
            counter.setCountDate(today);
            counter.setUpdatedAt(Instant.now());
            userActionCounterRepository.save(counter);
        }
    }

    // ======================== 徽章检查 ========================

    /**
     * 根据触发类型检查相关徽章。
     */
    private void checkBadges(Long userId, BadgeTriggerType triggerType, Object context) {
        switch (triggerType) {
            case ADD_ITEM -> {
                checkFirstItem(userId);
                checkDiamondButler(userId);
                checkNightOwl(userId);
                checkEarlyBird(userId);
            }
            case TAKE_OUT_ITEM -> {
                checkNightOwl(userId);
                checkEarlyBird(userId);
            }
            case TAKE_OUT_EXPIRING -> {
                checkProphet(userId);
                checkNightOwl(userId);
                checkEarlyBird(userId);
            }
            case UPDATE_ITEM -> {
                checkNightOwl(userId);
                checkEarlyBird(userId);
            }
            case CREATE_FRIDGE -> {
                checkFridgeMaster(userId);
            }
            case AI_CHAT -> {
                checkAiFriend(userId);
                checkNightOwl(userId);
                checkEarlyBird(userId);
            }
            case AI_RECIPE -> {
                checkChefCook(userId);
            }
            case VIEW_DATA_CENTER -> {
                checkDataMaster(userId);
            }
            case LOGIN -> {
                checkAnniversary(userId, context);
                checkNightOwl(userId);
                checkEarlyBird(userId);
            }
            case BIND_EMAIL -> {
                unlockBadge(userId, BadgeCode.SECURITY_EXPERT);
            }
            case COMPLETE_GUIDE -> {
                unlockBadge(userId, BadgeCode.GUIDE_COMPLETE);
            }
            case STREAK_CHECK -> {
                checkZeroWaste(userId, context);
                checkStreakGuardian(userId, context);
            }
            case FRESHNESS_SCORE -> {
                checkPerfectFreshness(userId, context);
            }
            case ORGANIZE -> {
                checkOrganizeExpert(userId);
            }
            default -> {
                // 无操作
            }
        }
    }

    // -------------------- 各徽章检查方法 --------------------

    private void checkFirstItem(Long userId) {
        long count = addRecordRepository.countByOperatorId(userId);
        if (count >= 1) {
            unlockBadge(userId, BadgeCode.FIRST_ITEM);
        }
    }

    private void checkFridgeMaster(Long userId) {
        long count = fridgeRepository.countByOwnerIdAndIsDeletedFalse(userId);
        if (count >= 3) {
            unlockBadge(userId, BadgeCode.FRIDGE_MASTER);
        }
    }

    private void checkZeroWaste(Long userId, Object context) {
        int streak = context instanceof Integer ? (Integer) context : 0;
        if (streak >= 30) {
            unlockBadge(userId, BadgeCode.ZERO_WASTE);
        }
    }

    private void checkProphet(Long userId) {
        UserActionCounter counter = userActionCounterRepository
                .findByUserIdAndCounterTypeAndCountDateIsNull(userId, COUNTER_PROPHET)
                .orElse(null);
        if (counter != null && counter.getCountValue() >= 10) {
            unlockBadge(userId, BadgeCode.PROPHET);
        }
    }

    private void checkChefCook(Long userId) {
        UserActionCounter counter = userActionCounterRepository
                .findByUserIdAndCounterTypeAndCountDateIsNull(userId, COUNTER_CHEF_COOK)
                .orElse(null);
        if (counter != null && counter.getCountValue() >= 10) {
            unlockBadge(userId, BadgeCode.CHEF_COOK);
        }
    }

    private void checkDataMaster(Long userId) {
        UserActionCounter counter = userActionCounterRepository
                .findByUserIdAndCounterTypeAndCountDateIsNull(userId, COUNTER_DATA_CENTER)
                .orElse(null);
        if (counter != null && counter.getCountValue() >= 50) {
            unlockBadge(userId, BadgeCode.DATA_MASTER);
        }
    }

    private void checkNightOwl(Long userId) {
        UserActionCounter counter = userActionCounterRepository
                .findByUserIdAndCounterTypeAndCountDateIsNull(userId, COUNTER_NIGHT_OWL)
                .orElse(null);
        if (counter != null && counter.getCountValue() >= 5) {
            unlockBadge(userId, BadgeCode.NIGHT_OWL);
        }
    }

    private void checkEarlyBird(Long userId) {
        UserActionCounter counter = userActionCounterRepository
                .findByUserIdAndCounterTypeAndCountDateIsNull(userId, COUNTER_EARLY_BIRD)
                .orElse(null);
        if (counter != null && counter.getCountValue() >= 5) {
            unlockBadge(userId, BadgeCode.EARLY_BIRD);
        }
    }

    private void checkAnniversary(Long userId, Object context) {
        SysUser user;
        if (context instanceof SysUser) {
            user = (SysUser) context;
        } else {
            user = sysUserRepository.findById(userId).orElse(null);
        }
        if (user == null || user.getCreateTime() == null) {
            return;
        }
        long days = ChronoUnit.DAYS.between(
                user.getCreateTime().atZone(ZONE_ID_SHANGHAI).toLocalDate(),
                LocalDate.now(ZONE_ID_SHANGHAI));
        if (days >= 365) {
            unlockBadge(userId, BadgeCode.ANNIVERSARY);
        }
    }

    private void checkStreakGuardian(Long userId, Object context) {
        int streak = context instanceof Integer ? (Integer) context : 0;
        if (streak >= 90) {
            unlockBadge(userId, BadgeCode.STREAK_GUARDIAN);
        }
    }

    private void checkPerfectFreshness(Long userId, Object context) {
        int score = context instanceof Integer ? (Integer) context : 0;
        if (score >= 100) {
            unlockBadge(userId, BadgeCode.PERFECT_FRESHNESS);
        }
    }

    private void checkDiamondButler(Long userId) {
        long count = addRecordRepository.countByOperatorId(userId);
        if (count >= 500) {
            unlockBadge(userId, BadgeCode.DIAMOND_BUTLER);
        }
    }

    private void checkAiFriend(Long userId) {
        UserActionCounter counter = userActionCounterRepository
                .findByUserIdAndCounterTypeAndCountDateIsNull(userId, COUNTER_AI_FRIEND)
                .orElse(null);
        if (counter != null && counter.getCountValue() >= 100) {
            unlockBadge(userId, BadgeCode.AI_FRIEND);
        }
    }

    private void checkOrganizeExpert(Long userId) {
        long dayCount = userActionCounterRepository.countByUserIdAndCounterType(userId, COUNTER_ORGANIZE_DAY);
        if (dayCount >= 5) {
            unlockBadge(userId, BadgeCode.ORGANIZE_EXPERT);
        }
    }
}
