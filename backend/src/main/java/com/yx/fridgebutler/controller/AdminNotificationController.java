package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.admin.ImportantNoticeBroadcastRequest;
import com.yx.fridgebutler.service.NotificationService;
import com.yx.fridgebutler.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/notification")
public class AdminNotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/broadcast")
    public Result<Void> broadcast(@Valid @RequestBody ImportantNoticeBroadcastRequest request) {
        notificationService.broadcastImportantNotice(request.getTitle(), request.getContent());
        log.info("管理员广播重要通知：{}", request.getTitle());
        return Result.success(null);
    }
}
