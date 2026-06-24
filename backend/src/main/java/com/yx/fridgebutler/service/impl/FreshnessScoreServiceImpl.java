package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.entity.BizFridge;
import com.yx.fridgebutler.entity.BizFridgeCapacityRate;
import com.yx.fridgebutler.entity.BizFridgeItem;
import com.yx.fridgebutler.entity.DailyFreshnessSnapshot;
import com.yx.fridgebutler.enums.BadgeTriggerType;
import com.yx.fridgebutler.enums.ExpActionType;
import com.yx.fridgebutler.repository.*;
import com.yx.fridgebutler.service.BadgeService;
import com.yx.fridgebutler.service.ExpService;
import com.yx.fridgebutler.service.FreshnessScoreService;
import com.yx.fridgebutler.vo.gamification.FreshnessScoreVO;
import com.yx.fridgebutler.vo.gamification.HeatmapDayVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 保鲜评分服务实现类。
 * <p>处理四维保鲜评分计算、每日快照保存、热力图数据查询、评分突破 EXP 发放等逻辑。</p>
 */
@Slf4j
@Service
public class FreshnessScoreServiceImpl implements FreshnessScoreService {

    /** 上海时区。 */
    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    /** 日期格式化器。 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** 周转效率理想比例下限。 */
    private static final double TURNOVER_IDEAL_MIN = 0.6;
    /** 周转效率理想比例上限。 */
    private static final double TURNOVER_IDEAL_MAX = 0.9;
    /** 空间利用理想比例下限。 */
    private static final double CAPACITY_IDEAL_MIN = 60.0;
    /** 空间利用理想比例上限。 */
    private static final double CAPACITY_IDEAL_MAX = 80.0;

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private BizFridgeItemRepository itemRepository;

    @Autowired
    private BizFridgeCapacityRateRepository capacityRateRepository;

    @Autowired
    private BizItemTakeOutRecordRepository takeOutRecordRepository;

    @Autowired
    private BizItemAddRecordRepository addRecordRepository;

    @Autowired
    private DailyFreshnessSnapshotRepository snapshotRepository;

    @Autowired
    private UserExpLogRepository userExpLogRepository;

    @Autowired
    private ExpService expService;

    @Autowired
    private BadgeService badgeService;

    /**
     * 自身代理引用，用于解决内部调用 {@code calculate} 时 Spring AOP 事务代理失效的问题。
     */
    @Lazy
    @Autowired
    private FreshnessScoreService self;

    /**
     * {@inheritDoc}
     * <p>
     * 按四个维度计算保鲜评分：
     * <ol>
     *   <li>新鲜度（40%）：R 值加权平均</li>
     *   <li>周转效率（30%）：近 30 天取出/入库比例</li>
     *   <li>过期控制（20%）：1 - 过期数/总数</li>
     *   <li>空间利用（10%）：容量利用率在 60%-80% 得满分</li>
     * </ol>
     * </p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FreshnessScoreVO calculate(Long userId) {
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);

        // 获取用户所有冰箱
        List<BizFridge> fridges = fridgeRepository.findByOwnerIdAndIsDeletedFalse(userId, Sort.unsorted());
        List<Long> fridgeIds = fridges.stream().map(BizFridge::getId).toList();

        // 获取所有未删除物品
        List<BizFridgeItem> allItems = new ArrayList<>();
        for (Long fridgeId : fridgeIds) {
            allItems.addAll(itemRepository.findByFridgeIdAndIsDeletedFalse(fridgeId));
        }

        // 过滤可用于评分的物品（有生产日期和保质期）
        List<BizFridgeItem> validItems = allItems.stream()
                .filter(i -> i.getProductionDate() != null && i.getShelfLifeDays() != null)
                .toList();

        // 没有可评分数据时（无冰箱/无物品/未填写有效期），不生成评分
        if (validItems.isEmpty()) {
            log.info("用户{}暂无有效食材数据，跳过保鲜评分计算", userId);
            return FreshnessScoreVO.builder()
                    .score(-1)
                    .grade("-")
                    .freshnessScore(0)
                    .turnoverScore(0)
                    .expiredControlScore(0)
                    .capacityScore(0)
                    .build();
        }

        // 计算各维度得分
        double freshnessScore = calculateFreshness(validItems, today);
        double turnoverScore = calculateTurnover(userId);
        double expiredControlScore = calculateExpiredControl(validItems, today);
        double capacityScore = calculateCapacity(fridgeIds);

        int total = (int) Math.round(
                freshnessScore * 0.4 + turnoverScore * 0.3 +
                        expiredControlScore * 0.2 + capacityScore * 0.1
        );
        total = Math.clamp(total, 0, 100);
        String grade = getScoreGrade(total);

        // 保存快照
        saveSnapshot(userId, today, total, grade, allItems, validItems, fridges,
                freshnessScore, turnoverScore, expiredControlScore, capacityScore);

        // 评分突破 EXP
        checkScoreBreakthrough(userId, total, grade, today);

        // 满分保鲜徽章检查
        badgeService.checkAndUnlock(userId, BadgeTriggerType.FRESHNESS_SCORE, total);

        log.info("用户{}保鲜评分计算完成：{}（{}），新鲜度{}，周转{}，过期控制{}，空间利用{}",
                userId, total, grade, freshnessScore, turnoverScore, expiredControlScore, capacityScore);

        return FreshnessScoreVO.builder()
                .score(total)
                .grade(grade)
                .freshnessScore(freshnessScore)
                .turnoverScore(turnoverScore)
                .expiredControlScore(expiredControlScore)
                .capacityScore(capacityScore)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FreshnessScoreVO getTodayScore(Long userId) {
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
        Optional<DailyFreshnessSnapshot> snapshot = snapshotRepository.findByUserIdAndSnapshotDate(userId, today);
        if (snapshot.isPresent()) {
            return convertToVO(snapshot.get());
        }
        return self.calculate(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HeatmapDayVO> getHeatmap(Long userId, int days) {
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
        LocalDate startDate = today.minusDays(days - 1L);

        List<DailyFreshnessSnapshot> snapshots = snapshotRepository
                .findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(userId, startDate, today);

        Map<LocalDate, DailyFreshnessSnapshot> snapshotMap = snapshots.stream()
                .collect(Collectors.toMap(DailyFreshnessSnapshot::getSnapshotDate, s -> s));

        List<HeatmapDayVO> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            DailyFreshnessSnapshot snapshot = snapshotMap.get(date);
            if (snapshot != null) {
                result.add(HeatmapDayVO.builder()
                        .date(date.format(DATE_FORMATTER))
                        .score(snapshot.getFreshnessScore() != null ? snapshot.getFreshnessScore() : 0)
                        .grade(snapshot.getScoreGrade() != null ? snapshot.getScoreGrade() : "-")
                        .hasExpired(snapshot.getHasExpired() != null && snapshot.getHasExpired() == 1)
                        .build());
            } else {
                result.add(HeatmapDayVO.builder()
                        .date(date.format(DATE_FORMATTER))
                        .score(-1)
                        .grade("-")
                        .hasExpired(false)
                        .build());
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScoreGrade(int score) {
        if (score >= 90) return "S";
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        return "D";
    }

    // ======================== 各维度评分计算 ========================

    /**
     * 计算新鲜度维度得分。
     * <p>R = (remainingDays / shelfLifeDays) * 100，取所有有效物品 R 值的平均值。</p>
     */
    private double calculateFreshness(List<BizFridgeItem> validItems, LocalDate today) {
        if (validItems.isEmpty()) {
            return 100.0;
        }
        double totalR = 0;
        for (BizFridgeItem item : validItems) {
            long diffDays = ChronoUnit.DAYS.between(item.getProductionDate(), today);
            int remainingDays = item.getShelfLifeDays() - (int) diffDays;
            double r = ((double) remainingDays / item.getShelfLifeDays()) * 100.0;
            totalR += Math.clamp(r, 0, 100);
        }
        return totalR / validItems.size();
    }

    /**
     * 计算周转效率维度得分。
     * <p>近 30 天取出数 / 近 30 天入库数，理想比例 0.6-0.9 得满分。</p>
     */
    private double calculateTurnover(Long userId) {
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
        Instant start = today.minusDays(29).atStartOfDay(ZONE_ID_SHANGHAI).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(ZONE_ID_SHANGHAI).toInstant();

        long takeOutCount = takeOutRecordRepository.countByOperatorIdAndCreateTimeBetween(userId, start, end);
        long addCount = countAddRecordsByOperatorIdAndTimeRange(userId, start, end);

        if (addCount == 0) {
            // 没有新增食材，视为满分（没有浪费）
            return 100.0;
        }

        double ratio = (double) takeOutCount / addCount;
        if (ratio >= TURNOVER_IDEAL_MIN && ratio <= TURNOVER_IDEAL_MAX) {
            return 100.0;
        }
        if (ratio < TURNOVER_IDEAL_MIN) {
            // 比例偏低，线性扣分
            return Math.max(0, ratio / TURNOVER_IDEAL_MIN * 100.0);
        }
        // 比例偏高，线性扣分
        double excess = ratio - TURNOVER_IDEAL_MAX;
        return Math.max(0, 100.0 - excess * 50.0);
    }

    /**
     * 统计指定用户在时间范围内的添加记录数量。
     */
    private long countAddRecordsByOperatorIdAndTimeRange(Long userId, Instant start, Instant end) {
        // 添加记录表没有直接的按时间范围计数方法，使用JPA派生查询
        return addRecordRepository.countByOperatorIdAndCreateTimeBetween(userId, start, end);
    }

    /**
     * 计算过期控制维度得分。
     */
    private double calculateExpiredControl(List<BizFridgeItem> validItems, LocalDate today) {
        if (validItems.isEmpty()) {
            return 100.0;
        }
        long expiredCount = validItems.stream()
                .filter(item -> isItemExpired(item, today))
                .count();
        return (1.0 - (double) expiredCount / validItems.size()) * 100.0;
    }

    /**
     * 计算空间利用维度得分。
     */
    private double calculateCapacity(List<Long> fridgeIds) {
        if (fridgeIds.isEmpty()) {
            return 100.0;
        }
        List<BizFridgeCapacityRate> rates = capacityRateRepository.findByFridgeIdIn(fridgeIds);
        if (rates.isEmpty()) {
            return 100.0;
        }
        double avgRate = rates.stream()
                .mapToInt(BizFridgeCapacityRate::getRate)
                .average()
                .orElse(0.0);

        if (avgRate >= CAPACITY_IDEAL_MIN && avgRate <= CAPACITY_IDEAL_MAX) {
            return 100.0;
        }
        if (avgRate < CAPACITY_IDEAL_MIN) {
            return Math.max(0, avgRate / CAPACITY_IDEAL_MIN * 100.0);
        }
        // 超过理想上限，线性扣分
        double excess = avgRate - CAPACITY_IDEAL_MAX;
        return Math.max(0, 100.0 - excess * 2.5);
    }

    // ======================== 辅助方法 ========================

    /**
     * 判断物品是否已过期。
     */
    private boolean isItemExpired(BizFridgeItem item, LocalDate today) {
        LocalDate expireDate = item.getProductionDate().plusDays(item.getShelfLifeDays());
        return expireDate.isBefore(today);
    }

    /**
     * 判断物品是否 3 天内临期（包含今天）。
     */
    private boolean isExpiringWithin3Days(BizFridgeItem item, LocalDate today) {
        LocalDate expireDate = item.getProductionDate().plusDays(item.getShelfLifeDays());
        long daysUntilExpire = ChronoUnit.DAYS.between(today, expireDate);
        return daysUntilExpire >= 0 && daysUntilExpire <= 3;
    }

    /**
     * 保存或更新当日快照。
     */
    private void saveSnapshot(Long userId, LocalDate today, int total, String grade,
                              List<BizFridgeItem> allItems, List<BizFridgeItem> validItems,
                              List<BizFridge> fridges,
                              double freshnessScore, double turnoverScore,
                              double expiredControlScore, double capacityScore) {
        DailyFreshnessSnapshot snapshot = snapshotRepository
                .findByUserIdAndSnapshotDate(userId, today)
                .orElse(new DailyFreshnessSnapshot());

        snapshot.setUserId(userId);
        snapshot.setSnapshotDate(today);
        snapshot.setFreshnessScore(total);
        snapshot.setScoreGrade(grade);
        snapshot.setFreshnessScoreFreshness(BigDecimal.valueOf(freshnessScore).setScale(2, RoundingMode.HALF_UP));
        snapshot.setFreshnessScoreTurnover(BigDecimal.valueOf(turnoverScore).setScale(2, RoundingMode.HALF_UP));
        snapshot.setFreshnessScoreExpired(BigDecimal.valueOf(expiredControlScore).setScale(2, RoundingMode.HALF_UP));
        snapshot.setFreshnessScoreCapacity(BigDecimal.valueOf(capacityScore).setScale(2, RoundingMode.HALF_UP));
        snapshot.setItemCount(allItems.size());
        snapshot.setFridgeCount(fridges.size());
        snapshot.setCreatedAt(Instant.now());

        if (!validItems.isEmpty()) {
            long expiredCount = validItems.stream()
                    .filter(item -> isItemExpired(item, today))
                    .count();
            long expiring3dCount = validItems.stream()
                    .filter(item -> isExpiringWithin3Days(item, today))
                    .count();
            snapshot.setExpiredCount((int) expiredCount);
            snapshot.setExpiring3dCount((int) expiring3dCount);
            snapshot.setHasExpired(expiredCount > 0 ? (byte) 1 : (byte) 0);
        } else {
            snapshot.setExpiredCount(0);
            snapshot.setExpiring3dCount(0);
            snapshot.setHasExpired((byte) 0);
        }

        if (!fridges.isEmpty()) {
            List<BizFridgeCapacityRate> rates = capacityRateRepository.findByFridgeIdIn(
                    fridges.stream().map(BizFridge::getId).toList());
            double avgRate = rates.stream()
                    .mapToInt(BizFridgeCapacityRate::getRate)
                    .average()
                    .orElse(0.0);
            snapshot.setCapacityRateAvg(BigDecimal.valueOf(avgRate).setScale(2, RoundingMode.HALF_UP));
        } else {
            snapshot.setCapacityRateAvg(BigDecimal.ZERO);
        }

        snapshotRepository.save(snapshot);
    }

    /**
     * 检查并发放评分突破 EXP。
     * <p>首次达到 S/A/B/C/D 每个等级各发放 1 次。</p>
     */
    private void checkScoreBreakthrough(Long userId, int total, String grade, LocalDate today) {
        // 如果该等级历史上已经有记录，说明不是首次达到
        long gradeCount = snapshotRepository.countByUserIdAndScoreGrade(userId, grade);
        if (gradeCount > 1) {
            // 当前快照保存前已经有该等级记录，不是首次
            return;
        }

        // 检查今日是否已发放过 SCORE_BREAKTHROUGH EXP（避免同一天多次调用重复发放）
        Instant start = today.atStartOfDay(ZONE_ID_SHANGHAI).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(ZONE_ID_SHANGHAI).toInstant();
        long todayCount = userExpLogRepository.countByUserIdAndActionTypeAndCreatedAtBetween(
                userId, ExpActionType.SCORE_BREAKTHROUGH.name(), start, end);
        if (todayCount > 0) {
            return;
        }

        expService.addExp(userId, ExpActionType.SCORE_BREAKTHROUGH, 30, null,
                "保鲜评分首次达到 " + grade + " 级（" + total + " 分）");
    }

    /**
     * 将快照实体转换为评分 VO。
     */
    private FreshnessScoreVO convertToVO(DailyFreshnessSnapshot snapshot) {
        return FreshnessScoreVO.builder()
                .score(snapshot.getFreshnessScore() != null ? snapshot.getFreshnessScore() : 0)
                .grade(snapshot.getScoreGrade() != null ? snapshot.getScoreGrade() : "-")
                .freshnessScore(getBigDecimalValue(snapshot.getFreshnessScoreFreshness()))
                .turnoverScore(getBigDecimalValue(snapshot.getFreshnessScoreTurnover()))
                .expiredControlScore(getBigDecimalValue(snapshot.getFreshnessScoreExpired()))
                .capacityScore(getBigDecimalValue(snapshot.getFreshnessScoreCapacity()))
                .build();
    }

    /**
     * 获取 BigDecimal 的 double 值，null 时返回 0。
     */
    private double getBigDecimalValue(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }
}
