package com.yx.fridgebutler.service;

import com.yx.fridgebutler.entity.UserExp;
import com.yx.fridgebutler.enums.ExpActionType;
import com.yx.fridgebutler.vo.gamification.ExpGainResult;
import com.yx.fridgebutler.vo.gamification.ExpLogVO;
import com.yx.fridgebutler.vo.gamification.LevelIconsVO;
import com.yx.fridgebutler.vo.gamification.LevelInfoVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 经验值服务接口。
 * <p>定义经验值增减、等级计算、每日上限控制等核心逻辑。</p>
 */
public interface ExpService {

    /**
     * 增加经验值（使用默认经验值）。
     *
     * @param userId     用户ID
     * @param actionType 行为类型
     */
    void addExp(Long userId, ExpActionType actionType);

    /**
     * 增加经验值并返回实际获得值（使用默认经验值）。
     *
     * @param userId     用户ID
     * @param actionType 行为类型
     * @return 经验值发放结果
     */
    ExpGainResult addExpWithResult(Long userId, ExpActionType actionType);

    /**
     * 增加经验值（自定义经验值）。
     *
     * @param userId     用户ID
     * @param actionType 行为类型
     * @param customExp  自定义经验值（null 则使用默认值）
     */
    void addExp(Long userId, ExpActionType actionType, Integer customExp);

    /**
     * 增加经验值并返回实际获得值（自定义经验值）。
     *
     * @param userId     用户ID
     * @param actionType 行为类型
     * @param customExp  自定义经验值（null 则使用默认值）
     * @return 经验值发放结果
     */
    ExpGainResult addExpWithResult(Long userId, ExpActionType actionType, Integer customExp);

    /**
     * 增加经验值（完整参数）。
     *
     * @param userId      用户ID
     * @param actionType  行为类型
     * @param customExp   自定义经验值（null 则使用默认值）
     * @param relatedId   关联业务ID
     * @param actionDesc  行为描述
     */
    void addExp(Long userId, ExpActionType actionType, Integer customExp, Long relatedId, String actionDesc);

    /**
     * 增加经验值并返回实际获得值（完整参数）。
     *
     * @param userId      用户ID
     * @param actionType  行为类型
     * @param customExp   自定义经验值（null 则使用默认值）
     * @param relatedId   关联业务ID
     * @param actionDesc  行为描述
     * @return 经验值发放结果
     */
    ExpGainResult addExpWithResult(Long userId, ExpActionType actionType, Integer customExp, Long relatedId, String actionDesc);

    /**
     * 增加经验值（完整参数），不校验每日经验值上限。
     * <p>用于历史徽章补发等需要确保经验值必须发放到位的场景。</p>
     *
     * @param userId      用户ID
     * @param actionType  行为类型
     * @param customExp   自定义经验值（null 则使用默认值）
     * @param relatedId   关联业务ID
     * @param actionDesc  行为描述
     */
    void addExpBypassDailyCap(Long userId, ExpActionType actionType, Integer customExp, Long relatedId, String actionDesc);

    /**
     * 获取或创建用户经验值记录。
     *
     * @param userId 用户ID
     * @return 用户经验值记录
     */
    UserExp getOrCreateUserExp(Long userId);

    /**
     * 获取用户等级信息。
     *
     * @param userId 用户ID
     * @return 等级信息 VO
     */
    LevelInfoVO getLevelInfo(Long userId);

    /**
     * 根据等级获取默认称号。
     *
     * @param level 等级
     * @return 称号
     */
    String getLevelTitle(int level);

    /**
     * 计算等级对应的 4 进制图标。
     *
     * @param level 等级
     * @return 图标 VO
     */
    LevelIconsVO calculateLevelIcons(int level);

    /**
     * 分页查询用户经验值日志。
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 经验值日志分页结果
     */
    Page<ExpLogVO> getExpLogPage(Long userId, Pageable pageable);

    /**
     * 获取每日经验值获取上限。
     *
     * @return 每日经验值上限
     */
    int getDailyExpCap();
}
