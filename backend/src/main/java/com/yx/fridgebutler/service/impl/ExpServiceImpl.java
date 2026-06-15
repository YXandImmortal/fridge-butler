package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.entity.UserExp;
import com.yx.fridgebutler.entity.UserExpLog;
import com.yx.fridgebutler.enums.ExpActionType;
import com.yx.fridgebutler.repository.UserExpLogRepository;
import com.yx.fridgebutler.repository.UserExpRepository;
import com.yx.fridgebutler.service.ExpService;
import com.yx.fridgebutler.vo.gamification.ExpGainResult;
import com.yx.fridgebutler.vo.gamification.LevelIconsVO;
import com.yx.fridgebutler.vo.gamification.LevelInfoVO;
import com.yx.fridgebutler.vo.gamification.NextLevelInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 经验值服务实现类。
 * <p>处理经验值增减、等级计算、每日上限控制、升级判定等核心逻辑。</p>
 */
@Slf4j
@Service
public class ExpServiceImpl implements ExpService {

    /** 上海时区。 */
    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    /** 预计算最大等级。 */
    private static final int MAX_PRECOMPUTE_LEVEL = 500;
    /** 等级升级所需经验值差值数组，index 为当前等级。 */
    private static final int[] LEVEL_REQUIRED_DIFF = new int[MAX_PRECOMPUTE_LEVEL + 1];
    /** 等级累计所需经验值数组，index 为目标等级。 */
    private static final int[] LEVEL_TOTAL_EXP = new int[MAX_PRECOMPUTE_LEVEL + 1];

    static {
        // Lv.1 初始值
        LEVEL_TOTAL_EXP[1] = 0;
        LEVEL_REQUIRED_DIFF[1] = 100;  // Lv.1→2
        LEVEL_REQUIRED_DIFF[2] = 200;  // Lv.2→3
        LEVEL_REQUIRED_DIFF[3] = 300;  // Lv.3→4
        LEVEL_REQUIRED_DIFF[4] = 400;  // Lv.4→5
        LEVEL_REQUIRED_DIFF[5] = 500;  // Lv.5→6
        LEVEL_REQUIRED_DIFF[6] = 700;  // Lv.6→7
        LEVEL_REQUIRED_DIFF[7] = 800;  // Lv.7→8

        // 预计算累计值（Lv.2 到 Lv.8）
        for (int i = 2; i <= 8; i++) {
            LEVEL_TOTAL_EXP[i] = LEVEL_TOTAL_EXP[i - 1] + LEVEL_REQUIRED_DIFF[i - 1];
        }

        // Lv.9 起使用公式：每级所需 EXP = 前一级所需 EXP + 50 × 当前等级
        for (int i = 8; i <= MAX_PRECOMPUTE_LEVEL; i++) {
            LEVEL_REQUIRED_DIFF[i] = LEVEL_REQUIRED_DIFF[i - 1] + 50 * i;
            if (i + 1 <= MAX_PRECOMPUTE_LEVEL) {
                LEVEL_TOTAL_EXP[i + 1] = LEVEL_TOTAL_EXP[i] + LEVEL_REQUIRED_DIFF[i];
            }
        }
    }

    /** 每日总 EXP 上限，从配置文件读取，默认 150。 */
    @Value("${gamification.exp.daily-cap:150}")
    private int dailyExpCap;

    /** AI 对话每日次数上限，从配置文件读取，默认 3。 */
    @Value("${gamification.exp.ai-chat-daily-limit:3}")
    private int aiChatDailyLimit;

    @Autowired
    private UserExpRepository userExpRepository;

    @Autowired
    private UserExpLogRepository userExpLogRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void addExp(Long userId, ExpActionType actionType) {
        addExpWithResult(userId, actionType, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExpGainResult addExpWithResult(Long userId, ExpActionType actionType) {
        return addExpWithResult(userId, actionType, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addExp(Long userId, ExpActionType actionType, Integer customExp) {
        addExpWithResult(userId, actionType, customExp, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExpGainResult addExpWithResult(Long userId, ExpActionType actionType, Integer customExp) {
        return addExpWithResult(userId, actionType, customExp, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addExp(Long userId, ExpActionType actionType, Integer customExp, Long relatedId, String actionDesc) {
        addExpWithResult(userId, actionType, customExp, relatedId, actionDesc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpGainResult addExpWithResult(Long userId, ExpActionType actionType, Integer customExp, Long relatedId, String actionDesc) {
        return doAddExp(userId, actionType, customExp, relatedId, actionDesc, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addExpBypassDailyCap(Long userId, ExpActionType actionType, Integer customExp, Long relatedId, String actionDesc) {
        doAddExp(userId, actionType, customExp, relatedId, actionDesc, true);
    }

    /**
     * 增加经验值核心逻辑。
     * <p>
     * 完整流程：
     * <ol>
     *   <li>获取或创建用户经验值记录</li>
     *   <li>检查并重置每日经验值（跨天）</li>
     *   <li>检查该行为的每日次数上限</li>
     *   <li>计算实际可获得的经验值</li>
     *   <li>增加经验值并检测升级</li>
     *   <li>保存记录并写入日志</li>
     * </ol>
     * </p>
     *
     * @param bypassDailyCap 为 true 时不校验每日总上限，且不累计到今日 EXP
     */
    private ExpGainResult doAddExp(Long userId, ExpActionType actionType, Integer customExp,
                                   Long relatedId, String actionDesc, boolean bypassDailyCap) {
        UserExp userExp = getOrCreateUserExp(userId);

        // 1. 检查并重置每日经验值
        resetDailyExpIfNeeded(userExp);

        ExpGainResult.ExpGainResultBuilder resultBuilder = ExpGainResult.builder()
                .expGained(0)
                .dailyExpToday(userExp.getDailyExpToday())
                .dailyExpLimit(dailyExpCap)
                .leveledUp(false)
                .currentLevel(userExp.getCurrentLevel());

        // 2. 确定本次获得的经验值
        int expValue = customExp != null ? customExp : (actionType.getExp() != null ? actionType.getExp() : 0);
        if (expValue <= 0) {
            return resultBuilder.build();
        }

        // 3. 检查一次性行为（历史是否已获得过）
        if (isOneTimeAction(actionType) && hasActionHistory(userId, actionType)) {
            log.debug("用户{}已获得过{}，跳过EXP发放", userId, actionType.name());
            return resultBuilder.build();
        }

        // 4. 检查行为次数上限（无上限或定时任务行为跳过）
        Integer dailyLimit = resolveDailyLimit(actionType);
        if (dailyLimit != null) {
            long todayCount = countTodayAction(userId, actionType.name());
            if (todayCount >= dailyLimit) {
                log.debug("用户{}今日{}已达上限{}，跳过EXP发放", userId, actionType.name(), dailyLimit);
                return resultBuilder.build();
            }
        }

        // 5. 受每日总上限限制（bypass 模式跳过）
        int actualExp;
        if (bypassDailyCap) {
            actualExp = expValue;
        } else {
            int remainingDailyCap = dailyExpCap - userExp.getDailyExpToday();
            if (remainingDailyCap <= 0) {
                log.debug("用户{}今日EXP已达总上限{}，跳过", userId, dailyExpCap);
                return resultBuilder.build();
            }
            actualExp = Math.min(expValue, remainingDailyCap);
        }

        // 6. 增加经验值
        int oldTotal = userExp.getTotalExp();
        userExp.setCurrentExp(userExp.getCurrentExp() + actualExp);
        userExp.setTotalExp(oldTotal + actualExp);
        if (!bypassDailyCap) {
            userExp.setDailyExpToday(userExp.getDailyExpToday() + actualExp);
        }

        // 7. 检测升级（可能连升多级）
        boolean leveledUp = false;
        while (checkLevelUp(userExp)) {
            leveledUp = true;
            log.info("用户{}升级至 Lv.{}，当前EXP：{}", userId, userExp.getCurrentLevel(), userExp.getCurrentExp());
        }

        userExpRepository.save(userExp);

        // 8. 记录日志
        UserExpLog expLog = new UserExpLog();
        expLog.setUserId(userId);
        expLog.setActionType(actionType.name());
        expLog.setActionDesc(actionDesc != null ? actionDesc : actionType.getDesc());
        expLog.setExpGained(actualExp);
        expLog.setExpBalance(userExp.getTotalExp());
        expLog.setRelatedId(relatedId);
        expLog.setCreatedAt(Instant.now());
        userExpLogRepository.save(expLog);

        if (bypassDailyCap) {
            log.info("用户{}获得{} EXP（绕过每日上限），行为：{}，当前等级：Lv.{}，当前EXP：{}，总EXP：{}",
                    userId, actualExp, actionType.name(), userExp.getCurrentLevel(),
                    userExp.getCurrentExp(), userExp.getTotalExp());
        } else {
            log.info("用户{}获得{} EXP，行为：{}，当前等级：Lv.{}，当前EXP：{}，今日EXP：{}/{}",
                    userId, actualExp, actionType.name(), userExp.getCurrentLevel(),
                    userExp.getCurrentExp(), userExp.getDailyExpToday(), dailyExpCap);
        }

        return resultBuilder
                .expGained(actualExp)
                .dailyExpToday(userExp.getDailyExpToday())
                .leveledUp(leveledUp)
                .currentLevel(userExp.getCurrentLevel())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserExp getOrCreateUserExp(Long userId) {
        return userExpRepository.findByUserId(userId).orElseGet(() -> {
            UserExp exp = new UserExp();
            exp.setUserId(userId);
            exp.setCurrentExp(0);
            exp.setTotalExp(0);
            exp.setCurrentLevel(1);
            exp.setDailyExpToday(0);
            exp.setDailyExpDate(LocalDate.now(ZONE_ID_SHANGHAI));
            exp.setCreatedAt(Instant.now());
            exp.setUpdatedAt(Instant.now());
            return userExpRepository.save(exp);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LevelInfoVO getLevelInfo(Long userId) {
        UserExp userExp = getOrCreateUserExp(userId);
        int level = userExp.getCurrentLevel();
        int required = getRequiredExpForNextLevel(level);
        double progress = required > 0
                ? BigDecimal.valueOf((double) userExp.getCurrentExp() / required * 100)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue()
                : 0.0;
        if (progress > 100.0) progress = 100.0;

        int nextLevel = level + 1;
        NextLevelInfoVO nextLevelInfo = NextLevelInfoVO.builder()
                .level(nextLevel)
                .title(getLevelTitle(nextLevel))
                .requiredExp(required)
                .icons(calculateLevelIcons(nextLevel))
                .build();

        return LevelInfoVO.builder()
                .currentLevel(level)
                .title(getLevelTitle(level))
                .currentExp(userExp.getCurrentExp())
                .requiredExp(required)
                .totalExp(userExp.getTotalExp())
                .icons(calculateLevelIcons(level))
                .progressPercent(progress)
                .nextLevel(nextLevelInfo)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLevelTitle(int level) {
        return switch (level) {
            case 1 -> "冰箱小白";
            case 2 -> "保鲜学徒";
            case 3 -> "食材管家";
            case 4 -> "零浪费达人";
            case 5 -> "冰箱大师";
            case 6 -> "智鲜宗师";
            case 7 -> "保鲜传奇";
            case 8 -> "冰鲜之神";
            default -> {
                // Lv.9+ 循环进阶，如 "冰鲜之神 II"
                int cycle = (level - 8);
                yield "冰鲜之神 " + toRoman(cycle);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LevelIconsVO calculateLevelIcons(int level) {
        int snowman = level / 64;
        int iceCream = (level % 64) / 16;
        int ice = (level % 16) / 4;
        int snowflake = level % 4;
        return LevelIconsVO.builder()
                .snowman(snowman)
                .iceCream(iceCream)
                .ice(ice)
                .snowflake(snowflake)
                .build();
    }

    /**
     * 检查并重置每日经验值（跨天时重置）。
     *
     * @param userExp 用户经验值记录
     */
    private void resetDailyExpIfNeeded(UserExp userExp) {
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
        if (userExp.getDailyExpDate() == null || !today.equals(userExp.getDailyExpDate())) {
            userExp.setDailyExpToday(0);
            userExp.setDailyExpDate(today);
        }
    }

    /**
     * 判断是否为一次性行为（终身只能获得一次 EXP）。
     *
     * @param actionType 行为类型
     * @return true 表示一次性行为
     */
    private boolean isOneTimeAction(ExpActionType actionType) {
        return actionType == ExpActionType.BIND_EMAIL
                || actionType == ExpActionType.GUIDE;
    }

    /**
     * 检查用户历史上是否已获得过指定行为的 EXP。
     *
     * @param userId     用户ID
     * @param actionType 行为类型
     * @return true 表示已有历史记录
     */
    private boolean hasActionHistory(Long userId, ExpActionType actionType) {
        return userExpLogRepository.countByUserIdAndActionType(userId, actionType.name()) > 0;
    }

    /**
     * 解析行为类型的每日次数上限。
     * <p>AI 对话的每日上限支持通过配置文件动态调整，其他行为仍使用枚举默认值。</p>
     *
     * @param actionType 行为类型
     * @return 每日次数上限，null 表示无上限
     */
    private Integer resolveDailyLimit(ExpActionType actionType) {
        if (actionType == ExpActionType.AI_CHAT) {
            return aiChatDailyLimit;
        }
        return actionType.getDailyLimit();
    }

    /**
     * 统计用户今日某行为类型的次数。
     *
     * @param userId     用户ID
     * @param actionType 行为类型
     * @return 今日次数
     */
    private long countTodayAction(Long userId, String actionType) {
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
        Instant start = today.atStartOfDay(ZONE_ID_SHANGHAI).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(ZONE_ID_SHANGHAI).toInstant();
        return userExpLogRepository.countByUserIdAndActionTypeAndCreatedAtBetween(userId, actionType, start, end);
    }

    /**
     * 检测并执行升级。
     *
     * @param userExp 用户经验值记录
     * @return 是否发生了升级
     */
    private boolean checkLevelUp(UserExp userExp) {
        int required = getRequiredExpForNextLevel(userExp.getCurrentLevel());
        if (required > 0 && userExp.getCurrentExp() >= required) {
            int overflow = userExp.getCurrentExp() - required;
            userExp.setCurrentLevel(userExp.getCurrentLevel() + 1);
            userExp.setCurrentExp(overflow);
            return true;
        }
        return false;
    }

    /**
     * 获取从指定等级升到下一级所需的经验值。
     *
     * @param currentLevel 当前等级
     * @return 升级所需经验值（差值）
     */
    private int getRequiredExpForNextLevel(int currentLevel) {
        if (currentLevel <= 0) return 0;
        if (currentLevel >= MAX_PRECOMPUTE_LEVEL) {
            // 超出预计算范围，使用公式推算
            int lastDiff = LEVEL_REQUIRED_DIFF[MAX_PRECOMPUTE_LEVEL];
            for (int i = MAX_PRECOMPUTE_LEVEL + 1; i <= currentLevel; i++) {
                lastDiff = lastDiff + 50 * i;
            }
            return lastDiff;
        }
        return LEVEL_REQUIRED_DIFF[currentLevel];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.springframework.data.domain.Page<com.yx.fridgebutler.vo.gamification.ExpLogVO> getExpLogPage(Long userId, org.springframework.data.domain.Pageable pageable) {
        var page = userExpLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return page.map(log -> com.yx.fridgebutler.vo.gamification.ExpLogVO.builder()
                .id(log.getId())
                .actionType(log.getActionType())
                .actionDesc(log.getActionDesc())
                .expGained(log.getExpGained())
                .expBalance(log.getExpBalance())
                .createdAt(log.getCreatedAt() != null
                        ? log.getCreatedAt().atZone(ZONE_ID_SHANGHAI).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : null)
                .build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDailyExpCap() {
        return dailyExpCap;
    }

    /**
     * 将阿拉伯数字转为罗马数字（用于 Lv.9+ 称号后缀）。
     *
     * @param num 数字
     * @return 罗马数字
     */
    private String toRoman(int num) {
        if (num <= 0) return "";
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
        return thousands[num / 1000] + hundreds[(num % 1000) / 100] + tens[(num % 100) / 10] + ones[num % 10];
    }
}
