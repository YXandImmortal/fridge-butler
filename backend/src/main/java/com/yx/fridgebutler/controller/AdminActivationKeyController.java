package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.admin.ActivationKeyGenerateRequest;
import com.yx.fridgebutler.dto.admin.ActivationKeyQueryRequest;
import com.yx.fridgebutler.service.AdminActivationKeyService;
import com.yx.fridgebutler.vo.PageResult;
import com.yx.fridgebutler.vo.Result;
import com.yx.fridgebutler.vo.admin.ActivationKeyVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员激活密钥控制器
 * <p>提供激活密钥的生成、查询、收回、销毁接口，仅超级管理员可访问。</p>
 */
@Slf4j
@RestController
@RequestMapping("/admin/activation-keys")
public class AdminActivationKeyController {

    @Autowired
    private AdminActivationKeyService adminActivationKeyService;

    /**
     * 分页查询激活密钥列表
     *
     * @param request 查询参数
     * @return 密钥分页列表
     */
    @GetMapping
    public Result<PageResult<ActivationKeyVO>> getKeyList(ActivationKeyQueryRequest request) {
        log.debug("管理员查询激活密钥列表，参数：{}", request);
        return Result.success(PageResult.of(adminActivationKeyService.getKeyList(request)));
    }

    /**
     * 批量生成激活密钥
     *
     * @param request 生成请求参数
     * @return 生成的密钥列表
     */
    @PostMapping
    public Result<List<ActivationKeyVO>> generateKeys(@Valid @RequestBody ActivationKeyGenerateRequest request) {
        log.info("管理员批量生成激活密钥，数量：{}，备注：{}", request.getCount(), request.getRemark());
        List<ActivationKeyVO> keys = adminActivationKeyService.generateKeys(request);
        return Result.success(keys);
    }

    /**
     * 发放激活密钥
     *
     * @param id 密钥ID
     * @return 操作结果
     */
    @PutMapping("/{id}/issue")
    public Result<Void> issueKey(@PathVariable Long id) {
        log.info("管理员发放激活密钥，ID：{}", id);
        adminActivationKeyService.issueKey(id);
        return Result.success(null);
    }

    /**
     * 收回激活密钥
     *
     * @param id 密钥ID
     * @return 操作结果
     */
    @PutMapping("/{id}/revoke")
    public Result<Void> revokeKey(@PathVariable Long id) {
        log.info("管理员收回激活密钥，ID：{}", id);
        adminActivationKeyService.revokeKey(id);
        return Result.success(null);
    }

    /**
     * 销毁激活密钥
     *
     * @param id 密钥ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> destroyKey(@PathVariable Long id) {
        log.info("管理员销毁激活密钥，ID：{}", id);
        adminActivationKeyService.destroyKey(id);
        return Result.success(null);
    }
}
