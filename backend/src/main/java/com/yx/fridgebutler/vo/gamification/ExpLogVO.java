package com.yx.fridgebutler.vo.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 经验值日志 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpLogVO {

    /** 日志ID */
    private Long id;

    /** 行为类型 */
    private String actionType;

    /** 行为描述 */
    private String actionDesc;

    /** 获得经验值 */
    private int expGained;

    /** 获得后的总经验值 */
    private int expBalance;

    /** 创建时间 */
    private String createdAt;
}
