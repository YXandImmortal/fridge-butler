package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.SidebarFeature;
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
    public static final String SYSTEM_VERSION = "alpha 0.0.2";
    public static final List<SidebarFeature> USER_INDEX_FEATURES;

    static {
        USER_INDEX_FEATURES = List.of(
                SidebarFeature.builder()
                        .id(1)
                        .name("首页")
                        .path("/user/index")
                        .icon("HomeFilled")
                        .build(),
                SidebarFeature.builder()
                        .id(2)
                        .name("冰箱管理")
                        .path("/user/fridge-management")
                        .icon("Refrigerator")
                        .build(),
                SidebarFeature.builder()
                        .id(3)
                        .name("物品分类")
                        .path("/user/item-category")
                        .icon("Goods")
                        .build(),
                SidebarFeature.builder()
                        .id(4)
                        .name("物品单位")
                        .path("/user/item-unit")
                        .icon("Box")
                        .build(),
                SidebarFeature.builder()
                        .id(5)
                        .name("个人中心")
                        .path("/user/center")
                        .icon("Avatar")
                        .build(),
                SidebarFeature.builder()
                        .id(6)
                        .name("关于系统")
                        .path("/system/about")
                        .icon("InfoFilled")
                        .build()
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
