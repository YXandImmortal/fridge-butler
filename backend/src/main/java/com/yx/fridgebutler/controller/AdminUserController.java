package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.admin.AdminUserQueryRequest;
import com.yx.fridgebutler.service.AdminUserService;
import com.yx.fridgebutler.vo.PageResult;
import com.yx.fridgebutler.vo.Result;
import com.yx.fridgebutler.vo.admin.AdminUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员用户管理控制器
 * <p>提供用户列表查询、详情查看、状态更新、密码重置等接口，仅管理员角色可访问。</p>
 */
@Slf4j
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    /**
     * 分页查询用户列表
     *
     * @param request 查询条件（关键词、状态、分页参数）
     * @return 用户分页列表
     */
    @GetMapping
    public Result<PageResult<AdminUserVO>> getUserList(AdminUserQueryRequest request) {
        log.debug("管理员查询用户列表，条件：{}", request);
        Page<AdminUserVO> page = adminUserService.getUserList(request);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查看用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}/detail")
    public Result<AdminUserVO> getUserDetail(@PathVariable Long id) {
        log.debug("管理员查询用户详情，用户ID：{}", id);
        return Result.success(adminUserService.getUserDetail(id));
    }

    /**
     * 更新用户状态（启用/禁用）
     *
     * @param id     用户ID
     * @param status 状态：true=禁用，false=正常
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Boolean status) {
        log.info("管理员更新用户状态，用户ID：{}，状态：{}", id, status);
        adminUserService.updateUserStatus(id, status);
        return Result.success(null);
    }

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @return 新生成的明文密码
     */
    @PostMapping("/{id}/reset-password")
    public Result<Map<String, String>> resetUserPassword(@PathVariable Long id) {
        log.info("管理员重置用户密码，用户ID：{}", id);
        String newPassword = adminUserService.resetUserPassword(id);
        return Result.success(Map.of("newPassword", newPassword));
    }
}
