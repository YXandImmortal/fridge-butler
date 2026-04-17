package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.SystemInfoDTO;
import com.yx.fridgebutler.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system")
public class SystemController {
    public static final String SYSTEM_NAME = "智鲜·引擎";
    public static final String SYSTEM_VERSION = "alpha 0.0.1";
    public static final List<String> USER_INDEX_FEATURES;

    static {
        USER_INDEX_FEATURES = List.of(
                "首页",
                "冰箱管理",
                "物品分类",
                "物品单位",
                "个人中心",
                "关于系统"
        );
    }

    @GetMapping("/info")
    public Result<SystemInfoDTO> getSystemInfo() {
        return Result.success(SystemInfoDTO.builder()
                .systemName(SYSTEM_NAME)
                .systemVersion(SYSTEM_VERSION)
                .userIndexFeatures(USER_INDEX_FEATURES)
                .build());
    }
}
