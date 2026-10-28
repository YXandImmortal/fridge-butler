package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.admin.AiConfigUpdateRequest;
import com.yx.fridgebutler.service.AiConfigService;
import com.yx.fridgebutler.service.AiRequestStatsService;
import com.yx.fridgebutler.vo.Result;
import com.yx.fridgebutler.vo.admin.AiConfigVO;
import com.yx.fridgebutler.vo.admin.AiProviderVO;
import com.yx.fridgebutler.vo.admin.AiUsageStatsVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/ai")
public class AdminAiController {

    @Autowired
    private AiConfigService aiConfigService;

    @Autowired
    private AiRequestStatsService requestStatsService;

    @GetMapping("/providers")
    public Result<List<AiProviderVO>> listProviders() {
        return Result.success(aiConfigService.listProviders());
    }

    @GetMapping("/config")
    public Result<AiConfigVO> getConfig() {
        return Result.success(aiConfigService.getConfig());
    }

    @PutMapping("/config")
    public Result<AiConfigVO> updateConfig(@Valid @RequestBody AiConfigUpdateRequest request) {
        log.info("管理员更新 AI 配置，厂商：{}，模型：{}", request.getProvider(), request.getModel());
        return Result.success(aiConfigService.updateConfig(request));
    }

    @GetMapping("/usage")
    public Result<AiUsageStatsVO> getUsage(@RequestParam(defaultValue = "7") Integer days) {
        return Result.success(requestStatsService.getUsage(days));
    }
}
