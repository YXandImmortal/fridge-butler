package com.yx.fridgebutler.vo.auth;

import com.yx.fridgebutler.vo.gamification.BadgeUnlockInfo;
import com.yx.fridgebutler.vo.gamification.LevelInfoVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    /**
     * 登录令牌
     */
    private String token;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否记住我
     */
    private Boolean rememberMe;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 是否已完成新手指引
     */
    private Boolean guideCompleted;

    /**
     * 是否需要修改初始密码（true=需要，false=不需要）
     */
    private Boolean requirePasswordChange;

    /**
     * 是否需要密钥激活（true=需要，false=不需要）
     * <p>当系统开启密钥激活且用户未激活时返回 true，前端应引导用户进入激活页面。</p>
     */
    private Boolean needActivation;

    /**
     * 用户是否已激活（true=已激活，false=未激活）
     */
    private Boolean isActivated;

    /**
     * 用户当前等级信息。
     */
    private LevelInfoVO level;

    /**
     * 本次登录是否在操作 EXP + 徽章 EXP 后触发升级。
     */
    private Boolean leveledUp;

    /**
     * 本次登录实际获得的经验值（今日首次登录为 5，否则为 0）。
     */
    private Integer expGained;

    /**
     * 今日已获得经验值。
     */
    private Integer dailyExpToday;

    /**
     * 每日经验值上限。
     */
    private Integer dailyExpLimit;

    /**
     * 本次登录新解锁的徽章列表。
     */
    @Builder.Default
    private List<BadgeUnlockInfo> badgesUnlocked = new ArrayList<>();
}