package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.admin.SystemConfigUpdateRequest;
import com.yx.fridgebutler.service.AdminSystemService;
import com.yx.fridgebutler.vo.Result;
import com.yx.fridgebutler.vo.admin.SystemConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员系统配置控制器
 * <p>提供系统配置的查询与更新接口，仅管理员角色可访问。</p>
 */
@Slf4j
@RestController
@RequestMapping("/admin/system")
public class AdminSystemController {

    @Autowired
    private AdminSystemService adminSystemService;

    /**
     * 获取系统配置
     *
     * @return 系统配置信息
     */
    @GetMapping("/config")
    public Result<SystemConfigVO> getSystemConfig() {
        log.debug("管理员查询系统配置");
        return Result.success(adminSystemService.getSystemConfig());
    }

    /**
     * 更新系统配置
     *
     * @param request 配置更新请求
     * @return 操作结果
     */
    @PutMapping("/config")
    public Result<Void> updateSystemConfig(@RequestBody SystemConfigUpdateRequest request) {
        log.info("管理员更新系统配置：{}", request);
        adminSystemService.updateSystemConfig(request);
        return Result.success(null);
    }
}
