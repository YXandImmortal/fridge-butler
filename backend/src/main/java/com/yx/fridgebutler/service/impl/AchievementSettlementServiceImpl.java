package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.entity.UserExp;
import com.yx.fridgebutler.service.AchievementSettlementService;
import com.yx.fridgebutler.service.BadgeService;
import com.yx.fridgebutler.service.ExpService;
import com.yx.fridgebutler.vo.gamification.AchievementSettlementResult;
import com.yx.fridgebutler.vo.gamification.BadgeTriggerRequest;
import com.yx.fridgebutler.vo.gamification.BadgeUnlockInfo;
import com.yx.fridgebutler.vo.gamification.ExpActionRequest;
import com.yx.fridgebutler.vo.gamification.ExpGainResult;
import com.yx.fridgebutler.vo.gamification.LevelInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 成就统一结算服务实现。
 */
@Slf4j
@Service
public class AchievementSettlementServiceImpl implements AchievementSettlementService {

    @Autowired
    private ExpService expService;

    @Autowired
    private BadgeService badgeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AchievementSettlementResult settle(
            Long userId,
            com.yx.fridgebutler.enums.ExpActionType actionType) {
        return settle(userId, List.of(new ExpActionRequest(actionType)), List.of());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AchievementSettlementResult settle(
            Long userId,
            com.yx.fridgebutler.enums.ExpActionType actionType,
            com.yx.fridgebutler.enums.BadgeTriggerType badgeTrigger,
            Object badgeContext) {
        return settle(
                userId,
                List.of(new ExpActionRequest(actionType)),
                List.of(new BadgeTriggerRequest(badgeTrigger, badgeContext)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AchievementSettlementResult settle(
            Long userId,
            com.yx.fridgebutler.enums.ExpActionType actionType,
            List<BadgeTriggerRequest> badgeTriggers) {
        return settle(userId, List.of(new ExpActionRequest(actionType)), badgeTriggers);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AchievementSettlementResult settle(
            Long userId,
            List<ExpActionRequest> actions,
            List<BadgeTriggerRequest> badgeTriggers) {
        if (actions == null) {
            actions = List.of();
        }
        if (badgeTriggers == null) {
            badgeTriggers = List.of();
        }

        // 1. 记录结算前等级
        UserExp userExpBefore = expService.getOrCreateUserExp(userId);
        int levelBefore = userExpBefore.getCurrentLevel();

        // 2. 发放所有直接行为 EXP
        int expGained = 0;
        int dailyExpToday = 0;
        int dailyExpLimit = expService.getDailyExpCap();
        for (ExpActionRequest action : actions) {
            ExpGainResult result = expService.addExpWithResult(
                    userId,
                    action.actionType(),
                    action.customExp(),
                    action.relatedId(),
                    action.actionDesc());
            expGained += result.getExpGained();
            dailyExpToday = result.getDailyExpToday();
            dailyExpLimit = result.getDailyExpLimit();
        }

        // 3. 触发徽章（unlockBadge 内部会再发放 BADGE EXP）
        List<BadgeUnlockInfo> badgesUnlocked = new ArrayList<>();
        for (BadgeTriggerRequest trigger : badgeTriggers) {
            badgesUnlocked.addAll(badgeService.checkAndUnlockWithResult(
                    userId, trigger.triggerType(), trigger.context()));
        }
        // 按徽章 code 去重，防止同一个徽章被多个 trigger 同时命中
        badgesUnlocked = badgesUnlocked.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(BadgeUnlockInfo::getCode, Function.identity(), (a, b) -> a),
                        map -> new ArrayList<>(map.values())));

        int badgeExpTotal = badgesUnlocked.stream()
                .mapToInt(BadgeUnlockInfo::getExpReward)
                .sum();

        // 4. 结算后取完整等级信息
        LevelInfoVO levelAfter = expService.getLevelInfo(userId);
        boolean leveledUp = levelAfter.getCurrentLevel() > levelBefore;

        if (expGained > 0 || !badgesUnlocked.isEmpty()) {
            log.info("成就结算完成，用户ID：{}，旧等级：Lv.{}，新等级：Lv.{}，直接EXP：{}，徽章EXP：{}，是否升级：{}",
                    userId, levelBefore, levelAfter.getCurrentLevel(),
                    expGained, badgeExpTotal, leveledUp);
        }

        return AchievementSettlementResult.builder()
                .expGained(expGained)
                .badgeExpTotal(badgeExpTotal)
                .totalExpGained(expGained + badgeExpTotal)
                .dailyExpToday(dailyExpToday)
                .dailyExpLimit(dailyExpLimit)
                .leveledUp(leveledUp)
                .currentLevel(levelAfter.getCurrentLevel())
                .level(levelAfter)
                .badgesUnlocked(badgesUnlocked)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AchievementSettlementResult settleBadgesOnly(
            Long userId,
            List<BadgeTriggerRequest> badgeTriggers) {
        return settle(userId, List.of(), badgeTriggers);
    }
}
