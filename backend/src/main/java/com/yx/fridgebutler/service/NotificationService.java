package com.yx.fridgebutler.service;

import com.yx.fridgebutler.entity.BizFridge;
import com.yx.fridgebutler.vo.notification.NotificationSummaryVO;
import com.yx.fridgebutler.vo.notification.NotificationVO;

import java.util.List;

/**
 * 消息通知服务接口。
 * <p>定义消息通知的查询、已读标记、删除以及临期提醒生成、容量预警等业务逻辑。</p>
 */
public interface NotificationService {

    /**
     * 查询当前登录用户的消息通知列表。
     *
     * @param type 消息类型筛选，为空时不筛选
     * @param page 页码，从1开始
     * @param size 每页数量
     * @return 消息通知列表
     */
    List<NotificationVO> listNotifications(String type, Integer status, int page, int size);

    /**
     * 获取当前登录用户的未读消息数量。
     *
     * @return 未读消息数量
     */
    Long getUnreadCount();

    /**
     * 获取当前登录用户的消息摘要统计。
     * <p>按类型统计各未读消息数量，用于前端角标展示。</p>
     *
     * @return 消息摘要统计
     */
    NotificationSummaryVO getSummary();

    /**
     * 标记指定消息为已读。
     *
     * @param id 消息ID
     */
    void markAsRead(Long id);

    /**
     * 标记当前用户的所有未读消息为已读。
     */
    void markAllAsRead();

    /**
     * 删除指定消息（软删除）。
     *
     * @param id 消息ID
     */
    void deleteNotification(Long id);

    /**
     * 生成临期/过期消息提醒。
     * <p>扫描所有用户的冰箱物品，根据保质期计算并生成对应的提醒通知。</p>
     */
    void generateExpiringNotifications();

    /**
     * 创建容量预警通知（如果不存在未读预警）。
     * <p>当冰箱容量利用率超过阈值时调用，避免重复生成同一冰箱的预警。</p>
     *
     * @param fridge 冰箱实体
     * @param rate   容量利用率百分比
     */
    void createCapacityWarningIfAbsent(BizFridge fridge, int rate);

    /**
     * 清除指定冰箱的容量预警通知。
     * <p>当冰箱容量利用率恢复到正常水平时调用，将对应的未读预警标记为已读。</p>
     *
     * @param fridgeId 冰箱ID
     */
    void clearCapacityWarning(Long fridgeId);

    /**
     * 广播重要通知给所有普通用户（非 SuperAdmin）。
     * <p>5 分钟内相同标题的通知将被拒绝，防止重复广播。</p>
     *
     * @param title   通知标题
     * @param content Markdown 通知内容
     */
    void broadcastImportantNotice(String title, String content);

    /**
     * 获取当前登录用户最新的未读重要通知。
     *
     * @return 最新的未读重要通知，不存在时返回 null
     */
    NotificationVO getLatestImportantNotice();

    /**
     * 为新注册用户初始化最新重要通知。
     * <p>从重要通知模板表中读取最新的一条未删除通知，为用户创建对应的个人通知记录。
     * 如果用户已存在未读的重要通知，则跳过避免重复。</p>
     *
     * @param userId 新注册用户ID
     */
    void initializeImportantNoticeForNewUser(Long userId);

    /**
     * 如果用户当天未收到过绑定邮箱提醒，则创建一条。
     * <p>用于用户登录后检测，当天仅提醒一次，避免过度打扰。</p>
     *
     * @param userId 用户ID
     */
    void createBindEmailReminderIfAbsent(Long userId);

    /**
     * 清除指定用户的绑定邮箱提醒通知。
     * <p>用户成功绑定邮箱后调用，将对应的未读提醒标记为已读。</p>
     *
     * @param userId 用户ID
     */
    void clearBindEmailReminder(Long userId);

    /**
     * 创建一条系统通知。
     * <p>用于账户安全事件、新用户欢迎等场景，由系统自动触发。</p>
     *
     * @param userId       用户ID
     * @param title        通知标题
     * @param content      通知内容
     * @param actionType   点击动作类型
     */
    void createSystemNotification(Long userId, String title, String content, String actionType);
}
