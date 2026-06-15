package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.gamification.MonthlyReportViewRequest;
import com.yx.fridgebutler.service.AchievementSettlementService;
import com.yx.fridgebutler.service.AchievementSettingService;
import com.yx.fridgebutler.service.ExpService;
import com.yx.fridgebutler.service.GamificationService;
import com.yx.fridgebutler.vo.gamification.AchievementSettlementResult;
import com.yx.fridgebutler.util.UserContextUtil;
import com.yx.fridgebutler.vo.PageResult;
import com.yx.fridgebutler.vo.Result;
import com.yx.fridgebutler.vo.gamification.AchievementSettingsVO;
import com.yx.fridgebutler.vo.gamification.BadgeVO;
import com.yx.fridgebutler.vo.gamification.DataCenterEnterVO;
import com.yx.fridgebutler.vo.gamification.ExpLogVO;
import com.yx.fridgebutler.vo.gamification.FreshnessScoreVO;
import com.yx.fridgebutler.vo.gamification.HeatmapDayVO;
import com.yx.fridgebutler.vo.gamification.MonthlyReportRewardVO;
import com.yx.fridgebutler.vo.gamification.MonthlyReportVO;
import com.yx.fridgebutler.vo.gamification.UserAchievementOverviewVO;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 游戏化成就控制器。
 * <p>处理个人中心成就相关的数据查询和设置更新。</p>
 */
@Slf4j
@RestController
@RequestMapping("/gamification")
public class GamificationController {

    /** 上海时区，用于时间格式化。 */
    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    /** 日期时间格式化器，格式为 yyyy-MM-dd HH:mm:ss。 */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private ExpService expService;

    @Autowired
    private AchievementSettingService achievementSettingService;

    /** 成就统一结算服务 */
    @Autowired
    private AchievementSettlementService achievementSettlementService;

    /**
     * 获取个人中心成就总览。
     *
     * @return 成就总览数据
     */
    @GetMapping("/overview")
    public Result<UserAchievementOverviewVO> getOverview() {
        UserAchievementOverviewVO result = gamificationService.getOverview();
        log.info("查询成就总览成功，用户ID：{}，等级：Lv.{}",
                UserContextUtil.getCurrentUserId(), result.getLevel().getCurrentLevel());
        return Result.success(result);
    }

    /**
     * 查询经验值日志（分页）。
     *
     * @param page 页码，从 0 开始
     * @param size 每页大小
     * @return 经验值日志分页列表
     */
    @GetMapping("/exp-log")
    public Result<PageResult<ExpLogVO>> getExpLog(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = UserContextUtil.getCurrentUserId();
        // 前端页码从 1 开始，Spring Data 页码从 0 开始
        int pageIndex = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var expLogPage = expService.getExpLogPage(userId, pageable);
        log.info("查询经验值日志成功，用户ID：{}，页码：{}，条数：{}", userId, page, expLogPage.getContent().size());
        return Result.success(PageResult.of(expLogPage));
    }

    /**
     * 查询徽章墙。
     *
     * @return 徽章列表
     */
    @GetMapping("/badges")
    public Result<List<BadgeVO>> getBadges() {
        List<BadgeVO> result = gamificationService.getUserBadges();
        log.info("查询徽章墙成功，用户ID：{}，徽章数量：{}",
                UserContextUtil.getCurrentUserId(), result.size());
        return Result.success(result);
    }

    /**
     * 查询当日实时保鲜评分。
     *
     * @return 保鲜评分
     */
    @GetMapping("/freshness-score")
    public Result<FreshnessScoreVO> getFreshnessScore() {
        FreshnessScoreVO result = gamificationService.getFreshnessScore();
        log.info("查询当日保鲜评分成功，用户ID：{}，评分：{}，等级：{}",
                UserContextUtil.getCurrentUserId(), result.getScore(), result.getGrade());
        return Result.success(result);
    }

    /**
     * 查询热力图数据。
     *
     * @param days 天数（90 / 180 / 365）
     * @return 热力图数据列表
     */
    @GetMapping("/heatmap")
    public Result<List<HeatmapDayVO>> getHeatmap(
            @RequestParam(defaultValue = "90") int days) {
        if (days != 90 && days != 180 && days != 365) {
            days = 90;
        }
        List<HeatmapDayVO> result = gamificationService.getHeatmap(days);
        log.info("查询热力图成功，用户ID：{}，天数：{}，数据条数：{}",
                UserContextUtil.getCurrentUserId(), days, result.size());
        return Result.success(result);
    }

    /**
     * 查询月度报告。
     * <p>仅返回报告数据，不触发 EXP/徽章结算。</p>
     *
     * @param yearMonth 年月，如 2026-05
     * @return 月度报告
     */
    @GetMapping("/monthly-report")
    public Result<MonthlyReportVO> getMonthlyReport(
            @RequestParam String yearMonth) {
        Long userId = UserContextUtil.getCurrentUserId();
        MonthlyReportVO result = gamificationService.getMonthlyReport(yearMonth);
        log.info("查询月度报告成功，用户ID：{}，年月：{}，报告存在：{}",
                userId, yearMonth, result != null);
        return Result.success(result);
    }

    /**
     * 查看月度报告结算。
     * <p>前端确认用户真正查看报告后调用，首次查看发放 EXP 并返回升级/徽章信息。</p>
     *
     * @param request 查看结算请求
     * @return 查看奖励信息
     */
    @PostMapping("/monthly-report/view")
    public Result<MonthlyReportRewardVO> viewMonthlyReport(
            @RequestBody @Valid MonthlyReportViewRequest request) {
        Long userId = UserContextUtil.getCurrentUserId();
        MonthlyReportRewardVO result = gamificationService.viewMonthlyReport(request.getYearMonth());
        log.info("月度报告查看结算，用户ID：{}，年月：{}，首次查看：{}，EXP：{}，是否升级：{}",
                userId, request.getYearMonth(), result.isFirstView(), result.getExpGained(), result.isLeveledUp());
        return Result.success(result);
    }

    /**
     * 进入数据中心。
     * <p>前端在用户进入数据中心页面时调用此接口，后端统一发放 VIEW_DATA_CENTER 经验值并触发徽章判定。</p>
     *
     * @return 经验值发放结果及新解锁徽章列表
     */
    @PostMapping("/data-center/enter")
    public Result<DataCenterEnterVO> enterDataCenter() {
        Long userId = UserContextUtil.getCurrentUserId();
        AchievementSettlementResult settlement = achievementSettlementService.settle(
                userId,
                com.yx.fridgebutler.enums.ExpActionType.VIEW_DATA_CENTER,
                com.yx.fridgebutler.enums.BadgeTriggerType.VIEW_DATA_CENTER,
                null);

        DataCenterEnterVO result = DataCenterEnterVO.builder()
                .expGained(settlement.getExpGained())
                .dailyExpToday(settlement.getDailyExpToday())
                .dailyExpLimit(settlement.getDailyExpLimit())
                .leveledUp(settlement.isLeveledUp())
                .currentLevel(settlement.getCurrentLevel())
                .level(settlement.getLevel())
                .badgesUnlocked(settlement.getBadgesUnlocked())
                .build();

        log.info("进入数据中心成功，用户ID：{}，获得EXP：{}，解锁徽章数：{}，是否升级：{}",
                userId, settlement.getExpGained(), settlement.getBadgesUnlocked().size(), settlement.isLeveledUp());
        return Result.success(result);
    }

    /**
     * 查询成就系统设置。
     *
     * @return 成就设置
     */
    @GetMapping("/settings")
    public Result<AchievementSettingsVO> getSettings() {
        Long userId = UserContextUtil.getCurrentUserId();
        AchievementSettingsVO result = achievementSettingService.getSettingVO(userId);
        log.info("查询成就设置成功，用户ID：{}，面板隐藏：{}", userId, result.getPanelHidden());
        return Result.success(result);
    }

    /**
     * 更新成就系统设置。
     *
     * @param request 设置更新请求
     * @return 操作结果
     */
    @PostMapping("/settings/update")
    public Result<Void> updateSettings(@RequestBody AchievementSettingsVO request) {
        Long userId = UserContextUtil.getCurrentUserId();
        achievementSettingService.updateSetting(userId, request);
        log.info("更新成就设置成功，用户ID：{}，面板隐藏：{}", userId, request.getPanelHidden());
        return Result.success(null);
    }
}
