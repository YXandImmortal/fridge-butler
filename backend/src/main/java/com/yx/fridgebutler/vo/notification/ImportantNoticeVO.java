package com.yx.fridgebutler.vo.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重要通知模板列表VO（管理员用）。
 * <p>用于展示管理员已创建的重要通知模板及其广播状态。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportantNoticeVO {

    /**
     * 通知模板ID
     */
    private Long id;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容（Markdown格式）
     */
    private String content;

    /**
     * 优先级：0普通 1警告 2紧急
     */
    private Integer priority;

    /**
     * 广播状态：ACTIVE=活跃，CLOSED=已关闭
     */
    private String status;

    /**
     * 广播次数
     */
    private Integer broadcastCount;

    /**
     * 最近一次广播时间（yyyy-MM-dd HH:mm:ss）
     */
    private String broadcastTime;

    /**
     * 创建时间（yyyy-MM-dd HH:mm:ss）
     */
    private String createTime;
}
