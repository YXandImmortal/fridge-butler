package com.yx.fridgebutler.vo.aichat;

import com.yx.fridgebutler.vo.gamification.BadgeUnlockInfo;
import com.yx.fridgebutler.vo.gamification.LevelInfoVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 聊天接口响应数据 VO。
 * <p>作为 {@link com.yx.fridgebutler.vo.Result} 的 data 字段内容。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatDataVO {

    /**
     * 对话会话ID，用于维持多轮对话上下文。
     */
    private String sessionId;

    /**
     * AI 回复内容，包含消息类型、文本和结构化数据。
     */
    private AIChatReplyVO reply;

    /**
     * 建议的下一步操作按钮文案列表。
     */
    private List<String> suggestions;

    /**
     * 本次 AI 对话实际获得的经验值。
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
     * 是否触发升级。
     */
    private Boolean leveledUp;

    /**
     * 升级后的新等级（未升级时与当前等级相同）。
     */
    private Integer currentLevel;

    /**
     * 结算后完整等级信息。
     */
    private LevelInfoVO level;

    /**
     * 本次对话新解锁的徽章列表。
     */
    @Builder.Default
    private List<BadgeUnlockInfo> badgesUnlocked = new ArrayList<>();
}
