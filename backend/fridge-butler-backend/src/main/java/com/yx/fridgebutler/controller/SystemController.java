package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.vo.AboutItemVO;
import com.yx.fridgebutler.vo.FeatureVO;
import com.yx.fridgebutler.vo.SidebarFeatureVO;
import com.yx.fridgebutler.vo.SystemInfoVO;
import com.yx.fridgebutler.vo.UpdateLogVO;
import com.yx.fridgebutler.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 系统信息控制器
 * <p>
 * 提供系统基本信息、功能特性、更新日志等静态数据。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/system")
public class SystemController {
    public static final String SYSTEM_NAME = "智鲜·引擎";
    public static final String SYSTEM_VERSION = "alpha 0.0.8";
    public static final String SYSTEM_SLOGAN = "智能管理您的冰箱，让食材更新鲜";
    public static final List<SidebarFeatureVO> USER_INDEX_FEATURES;
    public static final List<FeatureVO> SYSTEM_FEATURES;
    public static final List<UpdateLogVO> SYSTEM_UPDATES;
    public static final List<AboutItemVO> SYSTEM_ABOUT;

    static {
        USER_INDEX_FEATURES = List.of(
                SidebarFeatureVO.builder()
                        .id(1)
                        .name("首页")
                        .path("/user/index")
                        .icon("icon-home")
                        .build(),
                SidebarFeatureVO.builder()
                        .id(2)
                        .name("冰箱管理")
                        .path("/fridge")
                        .icon("icon-fridge-line")
                        .children(Arrays.asList(
                                SidebarFeatureVO.builder()
                                        .id(21)
                                        .name("冰箱一览")
                                        .path("/fridge/list")
                                        .build(),
                                SidebarFeatureVO.builder()
                                        .id(22)
                                        .name("创建冰箱")
                                        .path("/fridge/create")
                                        .build(),
                                SidebarFeatureVO.builder()
                                        .id(23)
                                        .name("详细信息")
                                        .path("/fridge/detail")
                                        .build(),
                                SidebarFeatureVO.builder()
                                        .id(24)
                                        .name("物品管理")
                                        .path("/fridge/items")
                                        .build()
                        ))
                        .build(),
                SidebarFeatureVO.builder()
                        .id(3)
                        .name("物品分类")
                        .path("/item-category")
                        .icon("icon-label-alt-multiple")
                        .children(Arrays.asList(
                                SidebarFeatureVO.builder()
                                        .id(31)
                                        .name("物品分类一览")
                                        .path("/item-category/list")
                                        .build(),
                                SidebarFeatureVO.builder()
                                        .id(32)
                                        .name("创建物品分类")
                                        .path("/item-category/create")
                                        .build()
                        ))
                        .build(),
                SidebarFeatureVO.builder()
                        .id(4)
                        .name("物品单位")
                        .path("/item-unit")
                        .icon("icon-inbox-all")
                        .children(Arrays.asList(
                                SidebarFeatureVO.builder()
                                        .id(41)
                                        .name("单位分类一览")
                                        .path("/item-unit-type/list")
                                        .build(),
                                SidebarFeatureVO.builder()
                                        .id(42)
                                        .name("创建单位分类")
                                        .path("/item-unit-type/create")
                                        .build()
                        ))
                        .build(),
                SidebarFeatureVO.builder()
                        .id(5)
                        .name("个人中心")
                        .path("/user/center")
                        .icon("icon-user")
                        .build(),
                SidebarFeatureVO.builder()
                        .id(6)
                        .name("关于系统")
                        .path("/user/about")
                        .icon("icon-info-box")
                        .build()
        );

        SYSTEM_FEATURES = List.of(
                FeatureVO.builder()
                        .title("冰箱管理")
                        .description("轻松管理多台冰箱，实时掌握冰箱状态，让食材存储井然有序")
                        .icon("icon-fridge-line")
                        .build(),
                FeatureVO.builder()
                        .title("物品分类")
                        .description("自定义物品分类体系，支持多级分类，让食材归类更清晰")
                        .icon("icon-label")
                        .build(),
                FeatureVO.builder()
                        .title("过期提醒")
                        .description("智能识别食材保质期，及时提醒即将过期的食材，有效减少浪费")
                        .icon("icon-notification")
                        .build(),
                FeatureVO.builder()
                        .title("数据统计")
                        .description("可视化数据报表，直观了解食材消耗情况，助您科学管理家庭饮食")
                        .icon("icon-chart-bar")
                        .build()
        );

        SYSTEM_UPDATES = List.of(
                UpdateLogVO.builder()
                        .version(SYSTEM_VERSION)
                        .date("2026-5-9")
                        .changes(Arrays.asList(
                                "完善了冰箱物品管理功能，现在可以使用全部物品管理功能",
                                "新增了物品分类管理功能，可以自由创建物品分类",
                                "新增了物品单位分类管理功能，物品数量一目了然"
                        ))
                        .build(),
                UpdateLogVO.builder()
                        .version("alpha 0.0.7")
                        .date("2026-4-30")
                        .changes(Arrays.asList(
                                "全面优化\"浅色\"与\"深色\"主题，界面更美观",
                                "物品管理功能上线，现在可以浏览与增加物品"
                        ))
                        .build(),
                UpdateLogVO.builder()
                        .version("alpha 0.0.6")
                        .date("2026-04-20")
                        .changes(Arrays.asList(
                                "新增系统颜色主题切换功能，预设\"浅色\"与\"深色\"主题",
                                "优化冰箱详情页交互体验",
                                "修复若干已知问题，提升系统稳定性",
                                "新增关于系统页面，介绍系统信息与功能"
                        ))
                        .build(),
                UpdateLogVO.builder()
                        .version("alpha 0.0.5")
                        .date("2026-04-15")
                        .changes(Arrays.asList(
                                "新增冰箱搜索功能，支持多条件筛选",
                                "支持用户头像上传与修改",
                                "优化系统性能，提升页面加载速度"
                        ))
                        .build(),
                UpdateLogVO.builder()
                        .version("alpha 0.0.4")
                        .date("2026-04-10")
                        .changes(Arrays.asList(
                                "新增用户个人中心页面",
                                "支持修改密码功能",
                                "优化登录页面视觉设计"
                        ))
                        .build()
        );

        SYSTEM_ABOUT = List.of(
                AboutItemVO.builder()
                        .label("技术支持")
                        .value("support@fridgebutler.com")
                        .type("email")
                        .build(),
                AboutItemVO.builder()
                        .label("官方网站")
                        .value("https://fridgebutler.example.com")
                        .type("url")
                        .build(),
                AboutItemVO.builder()
                        .label("版权所有")
                        .value("© 2026 智鲜引擎团队 版权所有")
                        .type("text")
                        .build(),
                AboutItemVO.builder()
                        .label("开源协议")
                        .value("MIT License")
                        .type("text")
                        .build()
        );
    }

    /**
     * 获取系统信息
     * <p>
     * 返回系统的完整信息，包括系统名称、版本、标语、功能特性、侧边栏菜单、更新日志和 关于我们信息。
     * </p>
     *
     * @return 包含系统所有信息的响应结果
     */
    @GetMapping("/info")
    public Result<SystemInfoVO> getSystemInfo() {
        log.debug("获取系统信息");
        return Result.success(SystemInfoVO.builder()
                .systemName(SYSTEM_NAME)
                .systemVersion(SYSTEM_VERSION)
                .slogan(SYSTEM_SLOGAN)
                .userIndexFeatures(USER_INDEX_FEATURES)
                .features(SYSTEM_FEATURES)
                .updates(SYSTEM_UPDATES)
                .about(SYSTEM_ABOUT)
                .build());
    }
}
