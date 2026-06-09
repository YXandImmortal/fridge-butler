package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.admin.ImportantNoticeBroadcastRequest;
import com.yx.fridgebutler.service.NotificationService;
import com.yx.fridgebutler.vo.PageResult;
import com.yx.fridgebutler.vo.Result;
import com.yx.fridgebutler.vo.notification.ImportantNoticeVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员通知控制器
 * <p>提供管理员向全体用户广播重要通知的接口，仅管理员角色可访问。</p>
 */
@Slf4j
@RestController
@RequestMapping("/admin/notification")
public class AdminNotificationController {

    /** 通知服务 */
    @Autowired
    private NotificationService notificationService;

    /**
     * 广播重要通知
     * <p>向所有用户推送重要通知消息。</p>
     *
     * @param request 广播请求参数，包含通知标题和内容
     * @return 操作结果
     */
    @PostMapping("/broadcast")
    public Result<Void> broadcast(@Valid @RequestBody ImportantNoticeBroadcastRequest request) {
        notificationService.broadcastImportantNotice(request.getTitle(), request.getContent());
        log.info("管理员广播重要通知：{}", request.getTitle());
        return Result.success(null);
    }

    /**
     * 查询重要通知模板列表
     * <p>管理员查看所有已创建的重要通知模板及其广播状态。</p>
     *
     * @param page 页码，从1开始，默认1
     * @param size 每页数量，默认10
     * @return 重要通知模板分页列表
     */
    @GetMapping("/important")
    public Result<PageResult<ImportantNoticeVO>> listImportantNotices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ImportantNoticeVO> list = notificationService.listImportantNotices(page, size);
        return Result.success(PageResult.of(list, (long) list.size()));
    }

    /**
     * 按ID广播/重新广播重要通知
     * <p>根据指定的重要通知模板ID，向所有普通用户重新推送该通知。</p>
     *
     * @param noticeId 重要通知模板ID
     * @return 操作结果
     */
    @PostMapping("/important/{noticeId}/broadcast")
    public Result<Void> broadcastById(@PathVariable Long noticeId) {
        notificationService.broadcastImportantNoticeById(noticeId);
        log.info("管理员按ID广播重要通知，模板ID：{}", noticeId);
        return Result.success(null);
    }

    /**
     * 关闭重要通知广播
     * <p>关闭后新注册用户将不再初始化此通知，已收到用户不受影响。</p>
     *
     * @param noticeId 重要通知模板ID
     * @return 操作结果
     */
    @PatchMapping("/important/{noticeId}/close")
    public Result<Void> closeImportantNotice(@PathVariable Long noticeId) {
        notificationService.closeImportantNotice(noticeId);
        log.info("管理员关闭重要通知广播，模板ID：{}", noticeId);
        return Result.success(null);
    }
}
