package com.yx.fridgebutler.vo.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息通知摘要统计VO
 * <p>用于前端角标/徽章展示各类型未读消息数量。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSummaryVO {

    /**
     * 总未读数
     */
    private Long totalUnread;

    /**
     * 已过期物品数量
     */
    private Long expiredCount;

    /**
     * 1天内过期物品数量
     */
    private Long expiringCriticalCount;

    /**
     * 3天内过期物品数量
     */
    private Long expiringWarningCount;

    /**
     * 7天内过期物品数量
     */
    private Long expiringNoticeCount;

    /**
     * 容量预警数量
     */
    private Long capacityWarningCount;
}
