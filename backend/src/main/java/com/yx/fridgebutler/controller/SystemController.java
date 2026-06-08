package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.service.AdminSystemService;
import com.yx.fridgebutler.vo.AboutItemVO;
import com.yx.fridgebutler.vo.FeatureVO;
import com.yx.fridgebutler.vo.PublicConfigVO;
import com.yx.fridgebutler.vo.SidebarFeatureVO;
import com.yx.fridgebutler.vo.SystemInfoVO;
import com.yx.fridgebutler.vo.UpdateLogVO;
import com.yx.fridgebutler.vo.Result;
import com.yx.fridgebutler.vo.admin.SystemConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AdminSystemService adminSystemService;

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
                    .version("release 0.4.0")
                    .date("2026-6-8")
                    .summary("重要更新：邮箱安全体系上线，通知系统全面升级")
                    .changes(Arrays.asList(
                            "邮箱安全体系上线：支持邮箱验证码注册、找回密码与绑定邮箱，账号安全全面升级",
                            "新增密码找回功能：通过邮箱验证码验证身份后可重置密码，忘记密码不再焦虑",
                            "通知系统全面升级：新增绑定邮箱提醒、系统通知、重要通知弹窗，支持快捷操作跳转",
                            "未绑定邮箱用户登录时自动收到安全提醒，点击即可跳转个人中心完成绑定",
                            "消息中心支持重要通知详情弹窗展示，避免遗漏管理员广播的关键信息",
                            "消息卡片支持快捷操作按钮：去绑定邮箱、去完成新手指引，操作一步直达",
                            "通知消息点击支持一键唤起快速上手指引，新用户上手更顺畅",
                            "明暗主题切换新增圆形扩散动画，视觉过渡更流畅自然",
                            "注册流程优化：邮箱验证码校验更精准，空值场景处理更完善",
                            "消息中心界面重构：布局更紧凑，操作按钮与消息卡片分离，交互更清晰",
                            "修复管理员后台看板日志耗时字段名错误导致数值不显示的问题",
                            "修复 AI 每日小贴士偶发空内容导致展示异常的问题"
                    ))
                    .isMajor(true)
                    .build(),
            UpdateLogVO.builder()
                    .version("release 0.3.1")
                    .date("2026-6-5")
                    .summary("体验优化与问题修复：激活码流程、错误提示与头像修复")
                    .changes(Arrays.asList(
                            "激活码验证后自动登录，无需再次输入账号密码",
                            "账号被禁用时新增专属提示页面，体验更友好",
                            "修复头像在某些环境下无法显示的问题",
                            "管理员后台激活码管理支持「发放」状态，密钥流转更清晰",
                            "系统错误提示全面升级，登录失败、无权访问等场景给出明确原因",
                            "后端接口统一 /api 前缀，部署更规范",
                            "多处交互细节与前端样式优化"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("release 0.3.0")
                    .date("2026-6-4")
                    .summary("重大版本升级：超级管理员后台与官网正式上线")
                    .changes(Arrays.asList(
                            "全新超级管理员后台上线，支持仪表盘、用户管理、日志审计、系统配置与通知广播",
                            "新增激活码系统，支持邀请制注册与内测用户管理",
                            "新增官网落地页，项目正式开源并对外开放访问",
                            "系统安全全面升级：首次登录强制修改初始密码、操作日志审计、JWT 安全增强",
                            "系统配置支持热更新，管理员可实时调整公告、注册开关等全局设置",
                            "注册后自动登录，无需二次输入账号密码，体验更流畅",
                            "前端组件架构全面重构，页面加载更快，交互更顺畅"
                    ))
                    .isMajor(true)
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.2.1")
                    .date("2026-5-27")
                    .summary("全新用户引导系统与头像焕新")
                    .changes(Arrays.asList(
                            "全新用户引导系统上线：首次登录自动播放全局布局引导，各核心页面支持分步教程",
                            "新增快速上手指南入口，随时重温系统引导",
                            "头像系统焕新，新增6款主题头像，界面更生动有趣",
                            "关于页面支持版本摘要展示与链接跳转，信息获取更便捷",
                            "多处UI细节与视觉体验优化，交互更流畅"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.2.0")
                    .date("2026-5-26")
                    .summary("AI 能力大升级：支持热量计算与智能物品创建")
                    .changes(Arrays.asList(
                            "新增 AI 热量计算，询问任意菜品即可获得热量与营养成分分析",
                            "新增 AI 物品创建向导，通过聊天一步步引导添加食材，更省心",
                            "AI 聊天支持引用冰箱或物品，回答更精准贴心",
                            "物品管理页支持「AI 帮我添加」一键触发创建向导",
                            "多处交互细节与视觉体验优化，使用更流畅"
                    ))
                    .isMajor(true)
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.1.6")
                    .date("2026-5-24")
                    .summary("AI 冰箱创建向导与提示词热更新")
                    .changes(Arrays.asList(
                            "新增 AI 冰箱创建向导，通过聊天引导完成冰箱创建",
                            "AI 提示词支持热更新，修改后无需重启服务即可生效",
                            "AI 引用冰箱时可感知过期/临期/新鲜等分类统计",
                            "数据展示逻辑优化，信息呈现更准确"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.1.5")
                    .date("2026-5-23")
                    .summary("全新消息通知系统上线")
                    .changes(Arrays.asList(
                            "新增消息通知系统，自动提醒临期/过期食材与容量预警",
                            "新增消息中心页面，支持按类型筛选、未读统计与一键已读",
                            "Header 通知图标支持下拉面板，快捷查看未读消息",
                            "AI 聊天支持引用附件上下文，回答关联实时数据",
                            "AI 菜谱推荐优先基于指定食材，更贴合用户需求"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.1.4")
                    .date("2026-5-21")
                    .summary("冰箱类型系统与新鲜度排序")
                    .changes(Arrays.asList(
                            "新增冰箱类型系统，支持单门/双门/三门/对开门等常见类型",
                            "物品管理新增新鲜度排序，直观查看食材新鲜程度",
                            "AI 容量估算关联冰箱类型，空间利用率更准确",
                            "搜索与筛选交互细节优化，操作更便捷"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.1.3")
                    .date("2026-5-21")
                    .summary("每日小贴士：AI 每日冷知识")
                    .changes(Arrays.asList(
                            "新增每日小贴士，AI 每日自动生成冰箱冷知识与实用技巧",
                            "侧边栏集成小贴士卡片，支持谜语式交互答题",
                            "无数据时实时调用 AI 生成并持久化，首次访问无需等待"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.1.2")
                    .date("2026-5-20")
                    .summary("AI 聊天流式输出与 Markdown 渲染")
                    .changes(Arrays.asList(
                            "AI 聊天支持 SSE 流式输出，实现打字机效果，响应更实时",
                            "AI 消息支持 Markdown 渲染与代码块语法高亮",
                            "AI 聊天支持随时中断当前对话",
                            "物品管理页容量卡片新增数据刷新频率提示"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.1.1")
                    .date("2026-5-19")
                    .summary("冰箱容量利用率智能统计")
                    .changes(Arrays.asList(
                            "新增冰箱容量利用率统计，基于 AI 智能估算空间占用",
                            "数据中心新增容量利用率仪表盘，直观展示各冰箱空间占用",
                            "物品管理页新增容量进度条，实时呈现已用容量占比",
                            "新增首次加载友好提示，避免等待时重复刷新"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.1.0")
                    .date("2026-5-19")
                    .summary("AI 智能冰箱管家正式上线")
                    .changes(Arrays.asList(
                            "全新上线 AI 智能冰箱管家，集成 DeepSeek 大模型",
                            "用户首页全面重构，新增个性化欢迎语与数据统计卡片",
                            "AI 聊天支持会话管理、历史回溯与结构化数据渲染",
                            "新增临期/过期物品统计摘要，实时展示食材保鲜状态",
                            "关于页面更新日志升级为 Timeline 时间轴展示"
                    ))
                    .isMajor(true)
                    .build(),
            UpdateLogVO.builder()
                    .version("alpha 0.0.9")
                    .date("2026-5-17")
                    .summary("创建交互优化与数据中心增强")
                    .changes(Arrays.asList(
                            "冰箱、分类、单位创建改为弹窗形式，操作更流畅",
                            "数据中心图表展示优化，数据呈现更精准",
                            "扩充系统图标库，界面表现力更丰富"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("alpha 0.0.8")
                    .date("2026-5-9")
                    .summary("物品分类与单位管理")
                    .changes(Arrays.asList(
                            "完善冰箱物品管理功能，支持全部物品管理操作",
                            "新增物品分类管理，自由创建与组织物品分类",
                            "新增物品单位管理，物品数量一目了然"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("alpha 0.0.7")
                    .date("2026-4-30")
                    .summary("主题优化与物品管理上线")
                    .changes(Arrays.asList(
                            "全面优化浅色与深色主题，界面视觉更美观",
                            "物品管理功能上线，支持浏览与增加物品"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("alpha 0.0.6")
                    .date("2026-04-20")
                    .summary("主题切换与关于页面")
                    .changes(Arrays.asList(
                            "新增浅色/深色主题切换功能，随心切换系统外观",
                            "优化冰箱详情页交互体验",
                            "新增关于系统页面，介绍系统信息与功能特性",
                            "修复若干已知问题，提升系统稳定性"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("alpha 0.0.5")
                    .date("2026-04-15")
                    .summary("冰箱搜索与头像上传")
                    .changes(Arrays.asList(
                            "新增冰箱搜索功能，支持多条件筛选",
                            "支持用户头像上传与修改",
                            "优化系统性能，提升页面加载速度"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("alpha 0.0.4")
                    .date("2026-04-10")
                    .summary("个人中心与密码修改")
                    .changes(Arrays.asList(
                            "新增用户个人中心页面",
                            "支持修改密码功能",
                            "优化登录页面视觉设计"
                    ))
                    .build()
    );

    private static final List<AboutItemVO> SYSTEM_ABOUT = List.of(
            AboutItemVO.builder()
                    .label("问题反馈")
                    .value("https://github.com/YXandImmortal/fridge-butler/issues/")
                    .type("url")
                    .icon("icon-debug")
                    .build(),
            AboutItemVO.builder()
                    .label("Github")
                    .value("https://github.com/YXandImmortal/fridge-butler/")
                    .type("url")
                    .icon("icon-github")
                    .build(),
            AboutItemVO.builder()
                    .label("版权所有")
                    .value("© 2026 智鲜引擎团队 版权所有")
                    .type("text")
                    .icon("icon-attachment")
                    .build(),
            AboutItemVO.builder()
                    .label("开源协议")
                    .value("AGPL-3.0")
                    .type("text")
                    .icon("icon-script-text")
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

    /**
     * 获取公开系统配置
     * <p>
     * 返回所有用户（含未登录）可访问的动态配置项，包括系统公告和系统简介。
     * </p>
     *
     * @return 公开配置信息
     */
    @GetMapping("/public-config")
    public Result<PublicConfigVO> getPublicConfig() {
        log.debug("获取公开系统配置");
        SystemConfigVO config = adminSystemService.getSystemConfig();
        return Result.success(PublicConfigVO.builder()
                .announcement(config.getAnnouncement())
                .systemDescription(config.getSystemDescription())
                .adminEmail(config.getAdminEmail())
                .build());
    }
}
