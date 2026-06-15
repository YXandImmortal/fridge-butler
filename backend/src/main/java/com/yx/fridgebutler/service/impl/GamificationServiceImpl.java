package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.entity.UserExp;
import com.yx.fridgebutler.enums.BadgeTriggerType;
import com.yx.fridgebutler.service.AchievementSettingService;
import com.yx.fridgebutler.service.BadgeService;
import com.yx.fridgebutler.service.ExpService;
import com.yx.fridgebutler.service.FreshnessScoreService;
import com.yx.fridgebutler.service.GamificationService;
import com.yx.fridgebutler.service.MonthlyReportService;
import com.yx.fridgebutler.service.StreakService;
import com.yx.fridgebutler.util.UserContextUtil;
import com.yx.fridgebutler.vo.gamification.AchievementSettingsVO;
import com.yx.fridgebutler.vo.gamification.BadgeUnlockInfo;
import com.yx.fridgebutler.vo.gamification.BadgeVO;
import com.yx.fridgebutler.vo.gamification.MonthlyReportRewardVO;
import com.yx.fridgebutler.vo.gamification.FreshnessDimensionVO;
import com.yx.fridgebutler.vo.gamification.FreshnessScoreVO;
import com.yx.fridgebutler.vo.gamification.HeatmapDayVO;
import com.yx.fridgebutler.vo.gamification.LevelInfoVO;
import com.yx.fridgebutler.vo.gamification.MonthlyReportVO;
import com.yx.fridgebutler.vo.gamification.StreakInfoVO;
import com.yx.fridgebutler.vo.gamification.UserAchievementOverviewVO;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 游戏化成就服务实现类。
 * <p>处理个人中心成就数据聚合查询逻辑。</p>
 */
@Slf4j
@Service
public class GamificationServiceImpl implements GamificationService {

    @Autowired
    private ExpService expService;

    @Autowired
    private StreakService streakService;

    @Autowired
    private AchievementSettingService achievementSettingService;

    @Autowired
    private BadgeService badgeService;

    @Autowired
    private FreshnessScoreService freshnessScoreService;

    @Autowired
    private MonthlyReportService monthlyReportService;

    /**
     * {@inheritDoc}
     * <p>
     * 聚合个人中心成就总览数据，包括等级、冰鲜连续天数、今日 EXP、设置等。
     * </p>
     */
    @Override
    public UserAchievementOverviewVO getOverview() {
        Long userId = UserContextUtil.getCurrentUserId();
        log.info("查询成就总览，用户ID：{}", userId);

        // 先记录触发评分计算前的等级和总 EXP
        UserExp userExpBefore = expService.getOrCreateUserExp(userId);
        int levelBefore = userExpBefore.getCurrentLevel();
        int totalExpBefore = userExpBefore.getTotalExp();

        // 先触发评分计算，可能发放评分突破 EXP 并解锁徽章
        FreshnessScoreVO scoreVO = freshnessScoreService.getTodayScore(userId);

        // 评分计算后再读取最新等级，确保返回的是升级后的状态
        LevelInfoVO levelInfo = expService.getLevelInfo(userId);
        StreakInfoVO streakInfo = streakService.getStreakInfo(userId);
        AchievementSettingsVO settings = achievementSettingService.getSettingVO(userId);
        List<BadgeVO> badges = badgeService.getUserBadges(userId);
        List<HeatmapDayVO> heatmap = freshnessScoreService.getHeatmap(userId, 365);

        UserExp userExpAfter = expService.getOrCreateUserExp(userId);

        // 计算本次 overview 请求实际产生的 EXP 收益
        int expGained = userExpAfter.getTotalExp() - totalExpBefore;

        // 触发并收集本次与评分相关的徽章解锁（idempotent，已解锁的不会重复处理）
        List<BadgeUnlockInfo> badgesUnlocked = badgeService.checkAndUnlockWithResult(
                userId, BadgeTriggerType.FRESHNESS_SCORE, scoreVO.getScore());

        if (expGained > 0 || !badgesUnlocked.isEmpty()) {
            log.info("成就总览触发奖励，用户ID：{}，旧等级：Lv.{}，新等级：Lv.{}，获得EXP：{}，解锁徽章数：{}",
                    userId, levelBefore, levelInfo.getCurrentLevel(), expGained, badgesUnlocked.size());
        }

        return UserAchievementOverviewVO.builder()
                .level(levelInfo)
                .streak(streakInfo)
                .badges(badges)
                .todayExp(userExpAfter.getDailyExpToday())
                .todayExpLimit(expService.getDailyExpCap())
                .freshnessScore(scoreVO.getScore())
                .scoreGrade(scoreVO.getGrade())
                .freshnessDimensions(buildFreshnessDimensions(scoreVO))
                .heatmap(heatmap)
                .settings(settings)
                .expGained(expGained)
                .leveledUp(levelBefore != levelInfo.getCurrentLevel())
                .badgesUnlocked(badgesUnlocked)
                .build();
    }

    /**
     * 根据保鲜评分 VO 构建四维评分明细。
     */
    private List<FreshnessDimensionVO> buildFreshnessDimensions(FreshnessScoreVO scoreVO) {
        return List.of(
                FreshnessDimensionVO.builder().label("新鲜度").score(clampScore(scoreVO.getFreshnessScore())).build(),
                FreshnessDimensionVO.builder().label("过期控制").score(clampScore(scoreVO.getExpiredControlScore())).build(),
                FreshnessDimensionVO.builder().label("周转效率").score(clampScore(scoreVO.getTurnoverScore())).build(),
                FreshnessDimensionVO.builder().label("空间利用").score(clampScore(scoreVO.getCapacityScore())).build()
        );
    }

    /**
     * 将 double 评分 Clamp 到 0-100 并转为 int。
     */
    private int clampScore(double score) {
        return Math.clamp((int) Math.round(score), 0, 100);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BadgeVO> getUserBadges() {
        Long userId = UserContextUtil.getCurrentUserId();
        log.info("查询徽章墙，用户ID：{}", userId);
        return badgeService.getUserBadges(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FreshnessScoreVO getFreshnessScore() {
        Long userId = UserContextUtil.getCurrentUserId();
        log.info("查询当日保鲜评分，用户ID：{}", userId);
        return freshnessScoreService.getTodayScore(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HeatmapDayVO> getHeatmap(int days) {
        Long userId = UserContextUtil.getCurrentUserId();
        log.info("查询热力图，用户ID：{}，天数：{}", userId, days);
        return freshnessScoreService.getHeatmap(userId, days);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MonthlyReportVO getMonthlyReport(String yearMonth) {
        Long userId = UserContextUtil.getCurrentUserId();
        log.info("查询月度报告，用户ID：{}，年月：{}", userId, yearMonth);
        return monthlyReportService.getReport(userId, yearMonth);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MonthlyReportRewardVO viewMonthlyReport(String yearMonth) {
        Long userId = UserContextUtil.getCurrentUserId();
        return monthlyReportService.markAsViewed(userId, yearMonth);
    }
}
