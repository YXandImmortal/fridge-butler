package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.vo.AboutItemVO;
import com.yx.fridgebutler.vo.FeatureVO;
import com.yx.fridgebutler.vo.SidebarFeatureVO;
import com.yx.fridgebutler.vo.SystemInfoVO;
import com.yx.fridgebutler.vo.UpdateLogVO;
import com.yx.fridgebutler.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${system.name}")
    private String systemName;

    @Value("${system.version}")
    private String systemVersion;

    @Value("${system.slogan}")
    private String systemSlogan;

    @Value("${system.build-time}")
    private String buildTime;

    private static final List<SidebarFeatureVO> USER_INDEX_FEATURES = List.of(
            SidebarFeatureVO.builder()
                    .id(1)
                    .name("首页")
                    .path("/user/index")
                    .icon("icon-home")
                    .build(),
            SidebarFeatureVO.builder()
                    .id(2)
                    .name("数据中心")
                    .path("/data-center/index")
                    .icon("icon-chart")
                    .build(),
            SidebarFeatureVO.builder()
                    .id(3)
                    .name("冰箱管理")
                    .path("/fridge")
                    .icon("icon-fridge-line")
                    .children(Arrays.asList(
                            SidebarFeatureVO.builder()
                                    .id(31)
                                    .name("冰箱一览")
                                    .path("/fridge/list")
                                    .build(),
                            SidebarFeatureVO.builder()
                                    .id(32)
                                    .name("详细信息")
                                    .path("/fridge/detail")
                                    .build(),
                            SidebarFeatureVO.builder()
                                    .id(33)
                                    .name("物品管理")
                                    .path("/fridge/items")
                                    .build()
                    ))
                    .build(),
            SidebarFeatureVO.builder()
                    .id(4)
                    .name("物品分类")
                    .path("/item-category/list")
                    .icon("icon-label-alt-multiple")
                    .build(),
            SidebarFeatureVO.builder()
                    .id(5)
                    .name("物品单位")
                    .path("/item-unit-type/list")
                    .icon("icon-inbox-all")
                    .build(),
            SidebarFeatureVO.builder()
                    .id(6)
                    .name("个人中心")
                    .path("/user/center")
                    .icon("icon-user")
                    .build(),
            SidebarFeatureVO.builder()
                    .id(7)
                    .name("关于系统")
                    .path("/user/about")
                    .icon("icon-info-box")
                    .build()
    );

    private static final List<FeatureVO> SYSTEM_FEATURES = List.of(
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

    private static final List<UpdateLogVO> SYSTEM_UPDATES = List.of(
            UpdateLogVO.builder()
                    .version("beta 0.1.0")
                    .date("2026-5-19")
                    .changes(Arrays.asList(
                            "全新上线 AI 智能冰箱管家，集成 DeepSeek 大模型，支持自然语言交互与意图识别",
                            "用户首页全面重构，新增个性化欢迎语、数据统计卡片与 AI 聊天助手入口",
                            "AI 聊天支持会话管理、历史消息回溯与结构化数据渲染（冰箱/物品/临期提醒）",
                            "新增临期/过期物品统计摘要接口，首页实时展示食材保鲜状态",
                            "后端 DTO 包结构按业务模块重构（auth、fridge、item、category 等），架构更清晰",
                            "安全配置升级：数据库密码、JWT 密钥等敏感信息强制环境变量注入，提升安全性",
                            "关于页面更新日志升级为 Timeline 时间轴展示，视觉体验更佳",
                            "系统版本正式进入 beta 阶段"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("alpha 0.0.9")
                    .date("2026-5-17")
                    .changes(Arrays.asList(
                            "冰箱创建、物品分类创建、物品单位创建改为弹窗形式，交互更流畅",
                            "优化 InputDialog 组件，支持无数据创建场景",
                            "数据中心图表展示优化，数据呈现更精准",
                            "后端系统配置改为动态注入，架构更灵活",
                            "扩充系统图标库，丰富界面表现力"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("alpha 0.0.8")
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

    private static final List<AboutItemVO> SYSTEM_ABOUT = List.of(
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
                .systemName(systemName)
                .systemVersion(systemVersion)
                .slogan(systemSlogan)
                .userIndexFeatures(USER_INDEX_FEATURES)
                .features(SYSTEM_FEATURES)
                .updates(SYSTEM_UPDATES)
                .about(SYSTEM_ABOUT)
                .build());
    }

    /**
     * 获取最新构建时间（版本标识）
     * <p>
     * 返回最新的构建时间标识，格式为 {@code yyyyMMdd-NN}（日期+构建次数），
     * 例如 {@code 20260517-01}。前端可通过比对该值判断当前系统是否为最新版本，
     * 若与本地缓存不一致则提示用户刷新页面或重新加载资源。
     * </p>
     *
     * @return 构建时间标识字符串
     */
    @GetMapping("/build-time")
    public Result<String> getBuildTime() {
        log.debug("获取系统构建时间");
        return Result.success(buildTime);
    }
}
