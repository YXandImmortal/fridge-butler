package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.admin.AdminLogQueryRequest;
import com.yx.fridgebutler.service.AdminLogService;
import com.yx.fridgebutler.vo.PageResult;
import com.yx.fridgebutler.vo.Result;
import com.yx.fridgebutler.vo.admin.AdminLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员操作日志控制器
 * <p>提供操作日志分页查询接口，仅管理员角色可访问。</p>
 */
@Slf4j
@RestController
@RequestMapping("/admin/logs")
public class AdminLogController {

    /** 管理员操作日志服务 */
    @Autowired
    private AdminLogService adminLogService;

    /**
     * 分页查询操作日志
     *
     * @param request 查询条件（用户名、URI、状态码、日期范围、分页）
     * @return 日志分页列表
     */
    @GetMapping
    public Result<PageResult<AdminLogVO>> getLogList(AdminLogQueryRequest request) {
        log.debug("管理员查询操作日志，条件：{}", request);
        return Result.success(adminLogService.getLogList(request));
    }
}
