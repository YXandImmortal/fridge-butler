package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.admin.ImportantNoticeBroadcastRequest;
import com.yx.fridgebutler.service.NotificationService;
import com.yx.fridgebutler.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
