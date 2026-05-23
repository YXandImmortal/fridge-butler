package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.entity.BizFridge;
import com.yx.fridgebutler.entity.BizFridgeItem;
import com.yx.fridgebutler.entity.SysNotification;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.enums.NotificationType;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.BizFridgeItemRepository;
import com.yx.fridgebutler.repository.BizFridgeRepository;
import com.yx.fridgebutler.repository.SysNotificationRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.NotificationService;
import com.yx.fridgebutler.vo.notification.NotificationSummaryVO;
import com.yx.fridgebutler.vo.notification.NotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知服务实现类。
 * <p>处理消息通知的查询、状态变更、删除以及临期提醒生成、容量预警管理等核心业务逻辑。</p>
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private SysNotificationRepository notificationRepository;

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private BizFridgeItemRepository itemRepository;

    @Autowired
    private SysUserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NotificationVO> listNotifications(String type, Integer status, int page, int size) {
        Long currentUserId = getCurrentUserId();
        log.info("查询消息列表，用户ID：{}，类型：{}，状态：{}，页码：{}，每页：{}", currentUserId, type, status, page, size);

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1));
        List<SysNotification> notifications = notificationRepository.findByUserIdAndTypeAndStatus(currentUserId, type, status, pageable);

        return notifications.stream()
                .map(this::convertToVO)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getUnreadCount() {
        Long currentUserId = getCurrentUserId();
        long count = notificationRepository.countUnreadByUserId(currentUserId);
        log.info("查询未读消息数，用户ID：{}，未读数：{}", currentUserId, count);
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationSummaryVO getSummary() {
        Long currentUserId = getCurrentUserId();
        log.info("查询消息摘要，用户ID：{}", currentUserId);

        return NotificationSummaryVO.builder()
                .totalUnread(notificationRepository.countUnreadByUserId(currentUserId))
                .expiredCount(notificationRepository.countUnreadByUserIdAndType(currentUserId, NotificationType.EXPIRED.name()))
                .expiringCriticalCount(notificationRepository.countUnreadByUserIdAndType(currentUserId, NotificationType.EXPIRING_CRITICAL.name()))
                .expiringWarningCount(notificationRepository.countUnreadByUserIdAndType(currentUserId, NotificationType.EXPIRING_WARNING.name()))
                .expiringNoticeCount(notificationRepository.countUnreadByUserIdAndType(currentUserId, NotificationType.EXPIRING_NOTICE.name()))
                .capacityWarningCount(notificationRepository.countUnreadByUserIdAndType(currentUserId, NotificationType.CAPACITY_WARNING.name()))
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>同时将阅读时间设为当前时间。</p>
     */
    @Override
    @Transactional
    public void markAsRead(Long id) {
        Long currentUserId = getCurrentUserId();
        log.info("标记消息已读，消息ID：{}，用户ID：{}", id, currentUserId);

        SysNotification notification = notificationRepository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(BusinessException::notificationNotFound);

        notification.setStatus((byte) 1);
        notification.setReadTime(Instant.now());
        notificationRepository.save(notification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void markAllAsRead() {
        Long currentUserId = getCurrentUserId();
        log.info("标记全部消息已读，用户ID：{}", currentUserId);

        int rows = notificationRepository.markAllAsReadByUserId(currentUserId);
        log.info("标记全部消息已读完成，用户ID：{}，更新数量：{}", currentUserId, rows);
    }

    /**
     * {@inheritDoc}
     * <p>采用软删除方式，仅将消息标记为已删除状态。</p>
     */
    @Override
    @Transactional
    public void deleteNotification(Long id) {
        Long currentUserId = getCurrentUserId();
        log.info("删除消息，消息ID：{}，用户ID：{}", id, currentUserId);

        int rows = notificationRepository.softDeleteByIdAndUserId(id, currentUserId);
        if (rows == 0) {
            throw BusinessException.notificationNotFound();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * 执行逻辑：
     * <ol>
     *   <li>查询所有未删除且启用的冰箱</li>
     *   <li>遍历每个冰箱中的物品，筛选出有生产日期和保质期天数的物品</li>
     *   <li>计算每个物品的保质期截止日期和剩余天数</li>
     *   <li>根据剩余天数判断提醒类型（已过期/1天内/3天内/7天内）</li>
     *   <li>检查去重条件（同一物品同一类型不重复生成未读提醒）</li>
     *   <li>生成并保存消息通知</li>
     * </ol>
     * </p>
     */
    @Override
    @Transactional
    public void generateExpiringNotifications() {
        log.info("定时任务开始：生成临期/过期消息提醒");
        LocalDate today = LocalDate.now();

        List<BizFridge> fridges = fridgeRepository.findAll().stream()
                .filter(f -> f.getIsDeleted() == null || !f.getIsDeleted())
                .filter(f -> f.getStatus() == null || f.getStatus())
                .toList();

        int createdCount = 0;

        for (BizFridge fridge : fridges) {
            List<BizFridgeItem> items = itemRepository.findByFridgeIdAndIsDeletedFalse(fridge.getId());
            for (BizFridgeItem item : items) {
                if (item.getProductionDate() == null || item.getShelfLifeDays() == null) {
                    continue;
                }

                LocalDate expireDate = item.getProductionDate().plusDays(item.getShelfLifeDays());
                long daysUntilExpire = ChronoUnit.DAYS.between(today, expireDate);

                NotificationType type = determineExpiringType(daysUntilExpire);
                if (type == null) {
                    continue;
                }

                boolean exists = notificationRepository.existsUnreadByUserIdAndItemIdAndType(
                        fridge.getOwnerId(), item.getId(), type.name());
                if (exists) {
                    continue;
                }

                String title = buildExpiringTitle(item, type, daysUntilExpire, expireDate);
                String content = buildExpiringContent(item, type, daysUntilExpire, expireDate);

                SysNotification notification = buildNotification(
                        fridge.getOwnerId(), fridge.getId(), item.getId(),
                        title, content, type, "VIEW_ITEM",
                        Map.of("itemId", item.getId(), "fridgeId", fridge.getId())
                );

                notificationRepository.save(notification);
                createdCount++;
            }
        }

        log.info("定时任务完成：生成临期/过期消息提醒，共创建 {} 条", createdCount);
    }

    /**
     * {@inheritDoc}
     * <p>如果该冰箱已存在未读的容量预警，则跳过不生成。</p>
     */
    @Override
    @Transactional
    public void createCapacityWarningIfAbsent(BizFridge fridge, int rate) {
        boolean exists = notificationRepository.existsUnreadByFridgeIdAndType(
                fridge.getId(), NotificationType.CAPACITY_WARNING.name());
        if (exists) {
            return;
        }

        String title = "冰箱容量预警";
        String content = String.format("「%s」空间利用率已达 %d%%，建议整理或清理", fridge.getFridgeName(), rate);

        SysNotification notification = buildNotification(
                fridge.getOwnerId(), fridge.getId(), null,
                title, content, NotificationType.CAPACITY_WARNING, "VIEW_FRIDGE",
                Map.of("fridgeId", fridge.getId())
        );

        notificationRepository.save(notification);
        log.info("创建容量预警通知，冰箱ID：{}，利用率：{}%", fridge.getId(), rate);
    }

    /**
     * {@inheritDoc}
     * <p>将指定冰箱所有未读的容量预警标记为已读。</p>
     */
    @Override
    @Transactional
    public void clearCapacityWarning(Long fridgeId) {
        int rows = notificationRepository.markCapacityWarningAsReadByFridgeId(
                fridgeId, NotificationType.CAPACITY_WARNING.name());
        if (rows > 0) {
            log.info("清除容量预警通知，冰箱ID：{}，清除数量：{}", fridgeId, rows);
        }
    }

    /**
     * 根据剩余天数判断临期提醒类型。
     *
     * @param daysUntilExpire 距离过期的剩余天数
     * @return 对应的通知类型，无需提醒时返回 null
     */
    private NotificationType determineExpiringType(long daysUntilExpire) {
        if (daysUntilExpire < 0) {
            return NotificationType.EXPIRED;
        } else if (daysUntilExpire <= 1) {
            return NotificationType.EXPIRING_CRITICAL;
        } else if (daysUntilExpire <= 3) {
            return NotificationType.EXPIRING_WARNING;
        } else if (daysUntilExpire <= 7) {
            return NotificationType.EXPIRING_NOTICE;
        }
        return null;
    }

    /**
     * 构建临期提醒标题。
     */
    private String buildExpiringTitle(BizFridgeItem item, NotificationType type, long days, LocalDate expireDate) {
        String dateStr = expireDate.format(DATE_FORMATTER);
        return switch (type) {
            case EXPIRED -> String.format("「%s」已过期（保质期至 %s）", item.getItemName(), dateStr);
            case EXPIRING_CRITICAL -> String.format("「%s」明天过期", item.getItemName());
            case EXPIRING_WARNING, EXPIRING_NOTICE ->
                    String.format("「%s」将在 %d 天后过期", item.getItemName(), days);
            default -> String.format("「%s」临期提醒", item.getItemName());
        };
    }

    /**
     * 构建临期提醒内容。
     */
    private String buildExpiringContent(BizFridgeItem item, NotificationType type, long days, LocalDate expireDate) {
        String dateStr = expireDate.format(DATE_FORMATTER);
        return switch (type) {
            case EXPIRED -> String.format("您的「%s」已于 %s 过期，请及时处理。", item.getItemName(), dateStr);
            case EXPIRING_CRITICAL ->
                    String.format("您的「%s」将于 %s 过期，请尽快使用。", item.getItemName(), dateStr);
            case EXPIRING_WARNING ->
                    String.format("您的「%s」保质期至 %s，还有 %d 天，请注意及时使用。", item.getItemName(), dateStr, days);
            case EXPIRING_NOTICE ->
                    String.format("您的「%s」保质期至 %s，还有 %d 天。", item.getItemName(), dateStr, days);
            default -> String.format("「%s」保质期至 %s。", item.getItemName(), dateStr);
        };
    }

    /**
     * 构建消息通知实体。
     */
    private SysNotification buildNotification(Long userId, Long fridgeId, Long itemId,
                                               String title, String content, NotificationType type,
                                               String actionType, Map<String, Object> actionPayload) {
        SysNotification notification = new SysNotification();
        notification.setUserId(userId);
        notification.setFridgeId(fridgeId);
        notification.setItemId(itemId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type.name());
        notification.setPriority((byte) type.getDefaultPriority());
        notification.setStatus((byte) 0);
        notification.setActionType(actionType);
        notification.setActionPayload(actionPayload != null ? new HashMap<>(actionPayload) : null);
        notification.setCreateTime(Instant.now());
        notification.setIsDeleted((byte) 0);
        return notification;
    }

    /**
     * 将消息通知实体转换为视图对象。
     */
    private NotificationVO convertToVO(SysNotification n) {
        NotificationType type = NotificationType.fromString(n.getType());
        return NotificationVO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .type(n.getType())
                .typeLabel(type != null ? type.getLabel() : n.getType())
                .priority(n.getPriority() != null ? n.getPriority().intValue() : 0)
                .status(n.getStatus() != null && n.getStatus() == 1 ? "READ" : "UNREAD")
                .actionType(n.getActionType())
                .actionPayload(n.getActionPayload())
                .createTime(formatInstant(n.getCreateTime()))
                .readTime(formatInstant(n.getReadTime()))
                .build();
    }

    /**
     * 将 Instant 格式化为上海时区的日期时间字符串。
     */
    private String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZONE_ID_SHANGHAI).format(DATE_TIME_FORMATTER);
    }

    /**
     * 获取当前登录用户的 ID。
     */
    private Long getCurrentUserId() {
        String username = getUsernameFromToken();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);
        return user.getId();
    }

    /**
     * 从 Spring Security 上下文中获取当前登录用户名。
     */
    private static String getUsernameFromToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw BusinessException.authFailed();
        }
        return authentication.getName();
    }
}
