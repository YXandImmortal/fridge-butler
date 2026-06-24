package com.yx.fridgebutler.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yx.fridgebutler.entity.*;
import com.yx.fridgebutler.enums.BadgeCode;
import com.yx.fridgebutler.enums.ExpActionType;
import com.yx.fridgebutler.repository.*;
import com.yx.fridgebutler.service.AchievementSettlementService;
import com.yx.fridgebutler.service.ExpService;
import com.yx.fridgebutler.service.MonthlyReportService;
import com.yx.fridgebutler.service.DeepSeekService;
import com.yx.fridgebutler.service.NotificationService;
import com.yx.fridgebutler.vo.gamification.AchievementSettlementResult;
import com.yx.fridgebutler.vo.gamification.BadgeUnlockInfo;
import com.yx.fridgebutler.vo.gamification.LevelInfoVO;
import com.yx.fridgebutler.vo.gamification.BadgeVO;
import com.yx.fridgebutler.vo.gamification.MonthlyReportRewardVO;
import com.yx.fridgebutler.vo.gamification.MonthlyReportVO;
import com.yx.fridgebutler.util.AiResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 月度报告服务实现类。
 * <p>处理月度报告生成、DeepSeek AI 浪费金额估算、环保价值换算、报告查询等逻辑。</p>
 */
@Slf4j
@Service
public class MonthlyReportServiceImpl implements MonthlyReportService {

    /** 上海时区。 */
    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    /** 年月格式化器。 */
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    /** 取出物品平均重量（kg），用于简化估算。 */
    private static final double AVG_ITEM_WEIGHT_KG = 0.3;
    /** CO₂ 换算系数。 */
    private static final double CO2_FACTOR = 2.5;
    /** 水换算系数。 */
    private static final double WATER_FACTOR = 1500.0;
    /** AI 估算失败时的默认单价（元/个）。 */
    private static final double DEFAULT_WASTE_PRICE = 5.0;
    /** AI 估算失败时的默认重量（kg/个）。 */
    private static final double DEFAULT_WASTE_KG = 0.2;

    /** AI 估算系统提示词。 */
    private static final String WASTE_ESTIMATE_SYSTEM_PROMPT = """
            你是一个冰箱食材价值估算助手。请根据用户提供的过期食材列表，估算这些食材的总浪费金额（人民币元）和总重量（kg）。
            估算要基于常见市场价格和食材重量，给出合理的近似值。
            只返回纯JSON格式，不要包含任何其他文字（包括markdown代码块标记、解释说明等）。
            返回格式：{"wastedAmount": 45.5, "wastedKg": 3.2}
            """;

    @Autowired
    private MonthlyReportRepository monthlyReportRepository;

    @Autowired
    private DailyFreshnessSnapshotRepository snapshotRepository;

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private BizFridgeItemRepository itemRepository;

    @Autowired
    private BizItemAddRecordRepository addRecordRepository;

    @Autowired
    private BizItemTakeOutRecordRepository takeOutRecordRepository;

    @Autowired
    private UserExpRepository userExpRepository;

    @Autowired
    private UserStreakRepository userStreakRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private ExpService expService;

    @Autowired
    private AchievementSettlementService achievementSettlementService;

    @Autowired
    private NotificationService notificationService;

    /**
     * {@inheritDoc}
     * <p>
     * 生成月度报告流程：
     * <ol>
     *   <li>解析年月为日期范围</li>
     *   <li>查询当月保鲜评分快照，计算评分趋势</li>
     *   <li>统计当月添加/取出记录数量</li>
     *   <li>统计当月过期/临期数量（从快照累加）</li>
     *   <li>获取月初/月末等级</li>
     *   <li>获取当月最高连续天数</li>
     *   <li>统计当月新解锁徽章数</li>
     *   <li>调用 DeepSeek AI 估算浪费金额和重量</li>
     *   <li>计算环保价值（CO₂、水）</li>
     *   <li>保存报告记录</li>
     * </ol>
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonthlyReportVO generate(Long userId, String yearMonth) {
        log.info("生成月度报告，用户ID：{}，年月：{}", userId, yearMonth);

        // 1. 解析年月为日期范围
        YearMonthRange range = parseYearMonth(yearMonth);

        // 2. 查询当月快照
        List<DailyFreshnessSnapshot> snapshots = snapshotRepository
                .findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(userId, range.startDate, range.endDate);

        int avgScore = 0, maxScore = 0, minScore = 0;
        int expiredCount = 0, expiringCount = 0;
        if (!snapshots.isEmpty()) {
            int totalScore = 0, count = 0;
            maxScore = snapshots.getFirst().getFreshnessScore() != null ? snapshots.getFirst().getFreshnessScore() : 0;
            minScore = maxScore;
            for (DailyFreshnessSnapshot s : snapshots) {
                Integer score = s.getFreshnessScore();
                if (score != null) {
                    totalScore += score;
                    count++;
                    if (score > maxScore) maxScore = score;
                    if (score < minScore) minScore = score;
                }
                expiredCount += s.getExpiredCount() != null ? s.getExpiredCount() : 0;
                expiringCount += s.getExpiring3dCount() != null ? s.getExpiring3dCount() : 0;
            }
            avgScore = count > 0 ? totalScore / count : 0;
        }

        // 3. 统计当月添加/取出数量
        Instant monthStart = range.startDate.atStartOfDay(ZONE_ID_SHANGHAI).toInstant();
        Instant monthEnd = range.endDate.plusDays(1).atStartOfDay(ZONE_ID_SHANGHAI).toInstant();
        long itemsAdded = addRecordRepository.countByOperatorIdAndCreateTimeBetween(userId, monthStart, monthEnd);
        long itemsTakenOut = takeOutRecordRepository.countByOperatorIdAndCreateTimeBetween(userId, monthStart, monthEnd);

        // 4. 获取月初/月末等级
        int levelStart = getLevelAtDate(userId, range.startDate);
        int levelEnd = getLevelAtDate(userId, range.endDate);

        // 5. 获取当月最高连续天数
        int streakMax = calculateMaxStreakInMonth(userId, snapshots);

        // 6. 统计当月新解锁徽章数
        int newBadges = countNewBadgesInMonth(userId, monthStart, monthEnd);

        // 7. AI 估算浪费金额和重量
        WasteEstimate estimate = estimateWasteByAI(userId, expiredCount);

        // 8. 计算环保价值
        double savedKg = Math.max(0, itemsTakenOut * AVG_ITEM_WEIGHT_KG);
        BigDecimal savedKgBd = BigDecimal.valueOf(savedKg).setScale(2, RoundingMode.HALF_UP);
        BigDecimal co2Saved = savedKgBd.multiply(BigDecimal.valueOf(CO2_FACTOR)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal waterSaved = savedKgBd.multiply(BigDecimal.valueOf(WATER_FACTOR)).setScale(2, RoundingMode.HALF_UP);

        // 9. 保存报告
        MonthlyReport report = monthlyReportRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .orElse(new MonthlyReport());
        report.setUserId(userId);
        report.setYearMonth(yearMonth);
        report.setAvgScore(avgScore);
        report.setMaxScore(maxScore);
        report.setMinScore(minScore);
        report.setExpiredCount(expiredCount);
        report.setExpiringCount(expiringCount);
        report.setWastedAmount(estimate.wastedAmount);
        report.setSavedKg(savedKgBd);
        report.setCo2Saved(co2Saved);
        report.setWaterSaved(waterSaved);
        report.setItemsAdded((int) itemsAdded);
        report.setItemsTakenOut((int) itemsTakenOut);
        report.setNewBadges(newBadges);
        report.setLevelStart(levelStart);
        report.setLevelEnd(levelEnd);
        report.setStreakMax(streakMax);
        report.setGeneratedAt(Instant.now());
        monthlyReportRepository.save(report);

        log.info("月度报告生成完成，用户ID：{}，年月：{}，浪费金额：{}，避免浪费：{}kg",
                userId, yearMonth, estimate.wastedAmount, savedKgBd);

        return convertToVO(report);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MonthlyReportVO getReport(Long userId, String yearMonth) {
        return monthlyReportRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .map(this::convertToVO)
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonthlyReportRewardVO markAsViewed(Long userId, String yearMonth) {
        MonthlyReport report = monthlyReportRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .orElse(null);
        if (report == null) {
            log.info("月度报告查看结算，报告不存在，用户ID：{}，年月：{}", userId, yearMonth);
            return buildEmptyRewardVO(userId);
        }

        // 首次查看发放 EXP
        if (report.getViewedAt() == null) {
            report.setViewedAt(Instant.now());
            monthlyReportRepository.save(report);
            AchievementSettlementResult settlement = achievementSettlementService.settle(
                    userId, ExpActionType.MONTHLY_REPORT);
            log.info("用户{}首次查看{}月度报告，发放 EXP：{}，是否升级：{}",
                    userId, yearMonth, settlement.getExpGained(), settlement.isLeveledUp());
            return convertToRewardVO(settlement, true);
        }

        log.info("月度报告查看结算，非首次查看，用户ID：{}，年月：{}", userId, yearMonth);
        return buildEmptyRewardVO(userId);
    }

    // ======================== 内部辅助方法 ========================

    /**
     * 解析年月字符串为日期范围。
     */
    private YearMonthRange parseYearMonth(String yearMonth) {
        LocalDate startDate = LocalDate.parse(yearMonth + "-01");
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return new YearMonthRange(startDate, endDate);
    }

    private record YearMonthRange(LocalDate startDate, LocalDate endDate) {
    }

    /**
     * 获取指定日期用户的等级（查询最近的 user_exp 记录，当前设计直接取当前等级）。
     */
    private int getLevelAtDate(Long userId, LocalDate date) {
        // 当前设计：user_exp 只保存最新状态，无法回溯历史等级。
        // 简化处理：返回当前等级。如需精确历史等级，需额外维护历史表。
        return userExpRepository.findByUserId(userId)
                .map(UserExp::getCurrentLevel)
                .orElse(1);
    }

    /**
     * 计算当月最高连续天数。
     * <p>基于当月每日快照的 current_streak 字段。由于快照中没有 streak 数据，
     * 这里简化为取月末 streak 或 user_streak 的当前值。</p>
     */
    private int calculateMaxStreakInMonth(Long userId, List<DailyFreshnessSnapshot> snapshots) {
        // 当前快照表不包含 streak 数据，简化处理：取用户当前 max_streak
        // 如需精确，后续可在 FreshnessSnapshotJob 中保存 streak 到快照
        return userStreakRepository.findByUserId(userId)
                .map(UserStreak::getMaxStreak)
                .orElse(0);
    }

    /**
     * 统计当月新解锁徽章数。
     */
    private int countNewBadgesInMonth(Long userId, Instant monthStart, Instant monthEnd) {
        List<UserBadge> badges = userBadgeRepository.findByUserId(userId);
        int count = 0;
        for (UserBadge badge : badges) {
            if (badge.getUnlockedAt() != null
                    && !badge.getUnlockedAt().isBefore(monthStart)
                    && badge.getUnlockedAt().isBefore(monthEnd)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 调用 DeepSeek AI 估算浪费金额和重量。
     *
     * @param userId      用户ID
     * @param expiredCount 当月过期总数
     * @return 估算结果
     */
    private WasteEstimate estimateWasteByAI(Long userId, int expiredCount) {
        if (expiredCount <= 0) {
            return new WasteEstimate(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // 收集当前过期物品作为样本
        List<BizFridge> fridges = fridgeRepository.findByOwnerIdAndIsDeletedFalse(userId, Sort.unsorted());
        List<String> itemLines = new ArrayList<>();
        int sampleCount = 0;
        for (BizFridge fridge : fridges) {
            List<BizFridgeItem> items = itemRepository.findByFridgeIdAndIsDeletedFalse(fridge.getId());
            LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
            for (BizFridgeItem item : items) {
                if (isItemExpired(item, today) && sampleCount < 10) {
                    String line = "- " + item.getItemName() + " " + item.getItemNum() + "个";
                    itemLines.add(line);
                    sampleCount++;
                }
            }
        }

        // 如果没有当前过期样本，使用通用描述
        String userPrompt;
        if (itemLines.isEmpty()) {
            userPrompt = "请估算一个家庭冰箱中一个月过期了 " + expiredCount
                    + " 个食材（包括蔬菜、水果、肉类、蛋奶等常见食材混合）的总浪费金额（人民币元）和总重量（kg）。";
        } else {
            userPrompt = "请估算以下过期食材列表的总浪费金额（人民币元）和总重量（kg）。\n"
                    + "本月共有 " + expiredCount + " 个食材过期，典型包括：\n"
                    + String.join("\n", itemLines);
        }

        try {
            String response = deepSeekService.chat(WASTE_ESTIMATE_SYSTEM_PROMPT, userPrompt);
            String cleaned = AiResponseUtils.cleanJsonResponse(response);
            JSONObject root = JSONUtil.parseObj(cleaned);
            double wastedAmount = root.getDouble("wastedAmount", expiredCount * DEFAULT_WASTE_PRICE);
            double wastedKg = root.getDouble("wastedKg", expiredCount * DEFAULT_WASTE_KG);

            // 如果有样本但样本数少于过期总数，按比例放大
            if (!itemLines.isEmpty() && sampleCount < expiredCount) {
                double ratio = (double) expiredCount / sampleCount;
                wastedAmount *= ratio;
                wastedKg *= ratio;
            }

            return new WasteEstimate(
                    BigDecimal.valueOf(wastedAmount).setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(wastedKg).setScale(2, RoundingMode.HALF_UP)
            );
        } catch (Exception e) {
            log.warn("DeepSeek AI 浪费金额估算失败，用户ID：{}，使用默认值", userId, e);
            return new WasteEstimate(
                    BigDecimal.valueOf(expiredCount * DEFAULT_WASTE_PRICE).setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(expiredCount * DEFAULT_WASTE_KG).setScale(2, RoundingMode.HALF_UP)
            );
        }
    }

    private record WasteEstimate(BigDecimal wastedAmount, BigDecimal wastedKg) {
    }

    /**
     * 判断物品是否已过期。
     */
    private boolean isItemExpired(BizFridgeItem item, LocalDate today) {
        if (item.getProductionDate() == null || item.getShelfLifeDays() == null) {
            return false;
        }
        LocalDate expireDate = item.getProductionDate().plusDays(item.getShelfLifeDays());
        return expireDate.isBefore(today);
    }

    /**
     * 将实体转换为 VO。
     */
    private MonthlyReportVO convertToVO(MonthlyReport report) {
        YearMonthRange range = parseYearMonth(report.getYearMonth());
        Instant monthStart = range.startDate.atStartOfDay(ZONE_ID_SHANGHAI).toInstant();
        Instant monthEnd = range.endDate.plusDays(1).atStartOfDay(ZONE_ID_SHANGHAI).toInstant();

        return MonthlyReportVO.builder()
                .yearMonth(report.getYearMonth())
                .avgScore(report.getAvgScore() != null ? report.getAvgScore() : 0)
                .maxScore(report.getMaxScore() != null ? report.getMaxScore() : 0)
                .minScore(report.getMinScore() != null ? report.getMinScore() : 0)
                .expiredCount(report.getExpiredCount() != null ? report.getExpiredCount() : 0)
                .expiringCount(report.getExpiringCount() != null ? report.getExpiringCount() : 0)
                .wastedAmount(report.getWastedAmount())
                .savedKg(report.getSavedKg())
                .co2Saved(report.getCo2Saved())
                .waterSaved(report.getWaterSaved())
                .itemsAdded(report.getItemsAdded() != null ? report.getItemsAdded() : 0)
                .itemsTakenOut(report.getItemsTakenOut() != null ? report.getItemsTakenOut() : 0)
                .newBadges(findNewBadgesInMonth(report.getUserId(), monthStart, monthEnd))
                .levelStart(report.getLevelStart() != null ? report.getLevelStart() : 1)
                .levelEnd(report.getLevelEnd() != null ? report.getLevelEnd() : 1)
                .streakMax(report.getStreakMax() != null ? report.getStreakMax() : 0)
                .viewed(report.getViewedAt() != null)
                .build();
    }

    /**
     * 构建空奖励 VO（报告不存在或已查看过）。
     */
    private MonthlyReportRewardVO buildEmptyRewardVO(Long userId) {
        UserExp userExp = expService.getOrCreateUserExp(userId);
        LevelInfoVO levelInfo = expService.getLevelInfo(userId);
        return MonthlyReportRewardVO.builder()
                .firstView(false)
                .expGained(0)
                .dailyExpToday(userExp.getDailyExpToday())
                .dailyExpLimit(expService.getDailyExpCap())
                .leveledUp(false)
                .level(levelInfo)
                .badgesUnlocked(new ArrayList<>())
                .build();
    }

    /**
     * 将成就结算结果转换为月度报告奖励 VO。
     */
    private MonthlyReportRewardVO convertToRewardVO(AchievementSettlementResult settlement, boolean firstView) {
        return MonthlyReportRewardVO.builder()
                .firstView(firstView)
                .expGained(settlement.getExpGained())
                .dailyExpToday(settlement.getDailyExpToday())
                .dailyExpLimit(settlement.getDailyExpLimit())
                .leveledUp(settlement.isLeveledUp())
                .level(settlement.getLevel())
                .badgesUnlocked(convertBadgeUnlockInfos(settlement.getBadgesUnlocked()))
                .build();
    }

    /**
     * 将徽章解锁信息列表转换为徽章 VO 列表。
     */
    private List<BadgeVO> convertBadgeUnlockInfos(List<BadgeUnlockInfo> unlockInfos) {
        if (unlockInfos == null || unlockInfos.isEmpty()) {
            return new ArrayList<>();
        }
        List<BadgeVO> result = new ArrayList<>(unlockInfos.size());
        Instant now = Instant.now();
        for (BadgeUnlockInfo info : unlockInfos) {
            BadgeCode badgeCode = BadgeCode.fromCode(info.getCode());
            result.add(BadgeVO.builder()
                    .code(info.getCode())
                    .name(info.getName())
                    .iconClass(info.getIconClass())
                    .description(info.getDescription())
                    .unlocked(true)
                    .unlockedAt(now)
                    .expReward(info.getExpReward())
                    .unlockConditionDesc(badgeCode != null ? badgeCode.getUnlockConditionDesc() : "")
                    .build());
        }
        return result;
    }

    /**
     * 查询指定月份新解锁的徽章列表。
     */
    private List<BadgeVO> findNewBadgesInMonth(Long userId, Instant monthStart, Instant monthEnd) {
        List<UserBadge> badges = userBadgeRepository.findByUserId(userId);
        List<BadgeVO> result = new ArrayList<>();
        for (UserBadge badge : badges) {
            if (badge.getUnlockedAt() != null
                    && !badge.getUnlockedAt().isBefore(monthStart)
                    && badge.getUnlockedAt().isBefore(monthEnd)) {
                result.add(convertUserBadgeToVO(badge));
            }
        }
        return result;
    }

    /**
     * 将用户徽章记录转换为徽章 VO。
     */
    private BadgeVO convertUserBadgeToVO(UserBadge userBadge) {
        BadgeCode badgeCode = BadgeCode.fromCode(userBadge.getBadgeCode());
        if (badgeCode == null) {
            return BadgeVO.builder()
                    .code(userBadge.getBadgeCode())
                    .name(userBadge.getBadgeCode())
                    .iconClass("")
                    .description("")
                    .unlocked(true)
                    .unlockedAt(userBadge.getUnlockedAt())
                    .expReward(0)
                    .unlockConditionDesc("")
                    .build();
        }
        return BadgeVO.builder()
                .code(badgeCode.getCode())
                .name(badgeCode.getName())
                .iconClass(badgeCode.getIconClass())
                .description(badgeCode.getDescription())
                .unlocked(true)
                .unlockedAt(userBadge.getUnlockedAt())
                .expReward(badgeCode.getExpReward())
                .unlockConditionDesc(badgeCode.getUnlockConditionDesc())
                .build();
    }
}
