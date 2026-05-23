package com.yx.fridgebutler.vo.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 消息通知响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型（英文标识）
     */
    private String type;

    /**
     * 消息类型显示名称
     */
    private String typeLabel;

    /**
     * 优先级：0普通 1警告 2紧急
     */
    private Integer priority;

    /**
     * 状态：UNREAD / READ
     */
    private String status;

    /**
     * 点击动作类型
     */
    private String actionType;

    /**
     * 动作参数
     */
    private Map<String, Object> actionPayload;

    /**
     * 创建时间（yyyy-MM-dd HH:mm:ss）
     */
    private String createTime;

    /**
     * 阅读时间（yyyy-MM-dd HH:mm:ss）
     */
    private String readTime;
}
