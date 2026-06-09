package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.service.NotificationService;
import com.yx.fridgebutler.vo.Result;
import com.yx.fridgebutler.vo.notification.NotificationSummaryVO;
import com.yx.fridgebutler.vo.notification.NotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息通知控制器
 * <p>
 * 处理消息通知的查询、已读标记、删除等操作。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/notification")
public class NotificationController {

    /** 通知服务 */
    @Autowired
    private NotificationService notificationService;

    /**
     * 查询当前登录用户的消息通知列表。
     *
     * @param type   消息类型筛选（可选）
     * @param status 消息状态筛选（可选），0=未读，1=已读
     * @param page   页码，默认1
     * @param size   每页数量，默认20
     * @return 消息通知列表
     */
    @GetMapping("/list")
    public Result<List<NotificationVO>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<NotificationVO> result = notificationService.listNotifications(type, status, page, size);
        log.info("查询消息列表成功，类型：{}，状态：{}，数量：{}", type, status, result.size());
        return Result.success(result);
    }

    /**
     * 获取当前登录用户的未读消息数量。
     *
     * @return 未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        Long count = notificationService.getUnreadCount();
        return Result.success(count);
    }

    /**
     * 获取当前登录用户的消息摘要统计。
     * <p>返回各类型未读消息数量，用于前端角标展示。</p>
     *
     * @return 消息摘要统计
     */
    @GetMapping("/summary")
    public Result<NotificationSummaryVO> summary() {
        NotificationSummaryVO result = notificationService.getSummary();
        return Result.success(result);
    }

    /**
     * 标记指定消息为已读。
     *
     * @param id 消息ID
     * @return 操作成功的响应结果
     */
    @PatchMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        log.info("标记消息已读，消息ID：{}", id);
        return Result.success(null);
    }

    /**
     * 标记当前用户的所有未读消息为已读。
     *
     * @return 操作成功的响应结果
     */
    @PatchMapping("/read-all")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        log.info("标记全部消息已读");
        return Result.success(null);
    }

    /**
     * 删除指定消息（软删除）。
     *
     * @param id 消息ID
     * @return 操作成功的响应结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        log.info("删除消息成功，消息ID：{}", id);
        return Result.success(null);
    }

    /**
     * 获取当前登录用户最新的未读重要通知。
     *
     * @return 最新的未读重要通知，不存在时返回 null
     */
    @GetMapping("/latest-important")
    public Result<NotificationVO> latestImportant() {
        NotificationVO result = notificationService.getLatestImportantNotice();
        return Result.success(result);
    }
}
