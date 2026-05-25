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
                    .version("beta 0.2.0")
                    .date("2026-5-26")
                    .changes(Arrays.asList(
                            "全新上线AI热量计算功能：支持基于引用物品/冰箱计算总热量，也可直接询问菜品热量，AI自动估算并给出营养成分分析",
                            "后端新增 calorie_calculation 意图识别与结构化数据处理：总热量、食材明细、营养成分、份量说明与AI总结",
                            "全新上线AI物品创建向导：用户可通过AI聊天以引导式交互添加物品，支持名称、分类、数量单位、保质期、备注、确认6步流程",
                            "后端新增 item_creation_wizard 意图识别与纯后端状态驱动推进机制，无需LLM参与向导流程，响应更稳定",
                            "前端新增 ItemCreationWizard 组件：步骤条、表单摘要、多种输入类型（文本/选择/组合单位/组合日期/文本域）与确认卡片",
                            "物品管理页新增「AI帮我添加」按钮接入向导，点击自动跳转AI聊天并携带当前冰箱ID触发创建流程",
                            "前端新增热量计算卡片UI：渐变总热量头部、食材明细列表、蛋白质/碳水/脂肪营养标签与AI总结",
                            "ChatStructuredMessage 全面支持 item_creation_wizard 与 calorie_calculation 消息类型渲染",
                            "关于页面更新日志支持重大更新标识：isMajor 字段驱动金色时间轴高亮与「重大更新」徽章展示",
                            "系统关于信息卡片支持图标展示，视觉层次更丰富",
                            "全局图标库扩充：新增物品、编辑框、保存、退出、勾选等图标，按钮与操作区视觉统一",
                            "物品管理页取出操作交互优化：下拉菜单升级为 Popover 浮层，避免层级遮挡与误触",
                            "多处按钮组件统一图标前缀与样式规范，移除重复CSS，维护性提升",
                            "AI聊天请求新增 fridgeId 字段，支持从物品管理页跳转时自动关联目标冰箱",
                            "意图识别提示词模板热更新支持 calorie_calculation 与 item_creation_wizard 两种新意图",
                            "前后端版本号同步升级至 beta 0.2.0"
                    ))
                    .isMajor(true)
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.1.6")
                    .date("2026-5-24")
                    .changes(Arrays.asList(
                            "全新上线AI冰箱创建向导：用户可通过AI聊天以引导式交互创建冰箱，支持名称、类型、容量、默认设置、地址与备注6步流程",
                            "后端新增 fridge_creation_wizard 意图识别与纯后端状态驱动推进机制，无需LLM参与向导流程，响应更稳定",
                            "前端新增 FridgeCreationWizard 组件：步骤条、表单摘要、多种输入类型（文本/选择/滑块/开关/组合输入）与确认卡片",
                            "冰箱列表页「AI帮我创建」按钮接入向导，点击自动跳转AI聊天并触发创建流程",
                            "新增 PromptTemplateLoader 提示词模板加载器：从 classpath:prompts/*.md 加载AI提示词，支持60秒定时热刷新",
                            "AI意图识别、通用对话、菜谱推荐、容量估算、每日小贴士等提示词全面模板化，修改无需重启服务",
                            "新增 AiResponseUtils 工具类：统一处理模型响应清洗与JSON子串提取，提升AI响应解析鲁棒性",
                            "DeepSeek服务接口增强：新增 chat(DeepSeekChatRequest) 与 chatStream(DeepSeekChatRequest)，支持自定义temperature与responseFormat",
                            "AI聊天历史新增滑动窗口截断策略：单会话默认保留最近10轮对话，避免上下文过长导致Token消耗激增",
                            "AI附件上下文增强：用户引用冰箱时，AI可感知已过期/临期/一般/新鲜/长保质期分类统计与具体物品明细",
                            "前端暗色模式下冰箱类型SVG图标自动反色，确保视觉可见性",
                            "Element Plus表单验证错误focus样式统一提取至全局覆盖，移除组件级重复代码",
                            "物品管理页容量卡片显示逻辑优化：改为展示基于利用率的已用容量（升），数据更准确",
                            "数据中心分类聚合逻辑修正：按物品种类计数替代数量累加，避免同一分类多数量物品失真",
                            "数据库初始化脚本全面补充：涵盖AI聊天会话/消息、冰箱容量利用率缓存、冰箱类型、通知、每日小贴士等全部新增表",
                            "前后端版本号同步升级至 beta 0.1.6"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.1.5")
                    .date("2026-5-23")
                    .changes(Arrays.asList(
                            "全新上线消息通知系统：支持临期/过期自动提醒、容量预警与系统通知，消息中心集中管理",
                            "后端新增完整消息通知模块：实体、枚举、Repository、Service、Controller 与定时任务",
                            "新增 NotificationGenerateJob 定时任务，每天凌晨 2:00 自动生成临期/过期消息提醒，自动去重",
                            "前端新增消息中心页面，支持按类型筛选、未读统计、一键已读与消息跳转",
                            "Header 通知图标升级为下拉面板，实时展示最近未读消息，支持快捷标记已读与跳转",
                            "前端新增通知 Store，支持 30 秒轮询未读数，消息状态实时同步",
                            "AI 聊天支持引用附件上下文：用户可引用冰箱或物品，AI 回答时关联实时业务数据",
                            "AI 菜谱推荐增强：优先基于用户指定的食材推荐，尊重用户数量要求，全量库存作为补充参考",
                            "AI 聊天空消息场景优化：仅携带附件时自动补充默认提示语",
                            "AI 消息历史支持附件快照还原，跨会话上下文完整性提升",
                            "物品新增记录实体新增 remark 备注字段，支持记录补充信息",
                            "前后端版本号同步升级至 beta 0.1.5"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.1.4")
                    .date("2026-5-21")
                    .changes(Arrays.asList(
                            "新增冰箱类型系统：支持单门、双门、三门、对开门、十字对开门等常见冰箱类型，创建与搜索时可选",
                            "冰箱创建弹窗、详情页编辑与列表搜索全面集成冰箱类型选择，支持类型图标可视化展示",
                            "AI 容量估算增强：估算时关联冰箱类型与备注信息，提升空间利用率估算准确度",
                            "容量统计缓存策略升级：冰箱类型变更将触发容量利用率重新计算，确保数据实时性",
                            "物品管理新增新鲜度排序：按 r 值（剩余天数/保质期×100）排序，直观查看食材新鲜程度",
                            "前端 CustomSelect 组件增强，支持 prefix 插槽与更灵活的选项展示",
                            "冰箱列表页新增搜索重置按钮，筛选操作更便捷",
                            "临期物品表、物品分类与单位管理页面交互细节优化",
                            "前后端版本号同步升级至 beta 0.1.4"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.1.3")
                    .date("2026-5-21")
                    .changes(Arrays.asList(
                            "全新上线每日小贴士功能：由 DeepSeek AI 每日自动生成冰箱相关的冷知识、实用技巧、冷笑话与谜语",
                            "后端新增每日小贴士完整模块：实体、枚举、Repository、Service、Controller 与定时任务",
                            "新增 /daily-tip/today 与 /daily-tip/by-date 接口，支持匿名访问，无数据时实时调用 AI 生成并持久化",
                            "新增 DailyTipGenerateJob 定时任务，每天凌晨 1:00 预生成当日小贴士，避免首次访问等待",
                            "前端侧边栏集成每日小贴士卡片，支持类型标签、emoji、谜语交互式答案与本地缓存",
                            "前端新增 dailyTip.js API 模块与 DAILY_TIP_API.md 接口文档",
                            "AI 聊天用户消息气泡背景色优化，提升视觉辨识度",
                            "用户首页欢迎区与消息时间样式微调，整体排版更紧凑",
                            "Sidebar 布局重构，适配底部固定小贴士区域，导航区独立滚动"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.1.2")
                    .date("2026-5-20")
                    .changes(Arrays.asList(
                            "AI 聊天全面升级：支持 SSE 流式输出，实现逐字打字机效果，响应更实时",
                            "AI 消息支持 Markdown 渲染与代码块语法高亮，提升技术类回答的可读性",
                            "前端新增 AiMessageContent 组件，封装 Markdown 解析与 Highlight.js 高亮",
                            "后端新增 /ai/chat/stream 流式接口，基于 SseEmitter 分阶段推送 text/card/done 事件",
                            "DeepSeek 服务接入流式对话 API（chatStream），支持逐字符实时获取模型回复",
                            "AI 聊天支持中断请求（AbortController），用户可随时终止当前对话",
                            "物品管理页冰箱容量卡片新增数据刷新频率提示，交互更友好",
                            "安全配置优化：放行 ASYNC/ERROR 分发类型，确保 SSE 异步请求正常处理",
                            "后端代码现代化：IntentResult 改为 record 类型，Math.clamp 替代手动边界计算",
                            "新增 SSE 使用指南文档（SSE_USAGE_GUIDE.md），前后端各一份"
                    ))
                    .build(),
            UpdateLogVO.builder()
                    .version("beta 0.1.1")
                    .date("2026-5-19")
                    .changes(Arrays.asList(
                            "新增冰箱容量利用率统计功能，基于 DeepSeek AI 大模型智能估算各冰箱空间占用率",
                            "后端新增容量统计服务与缓存机制，支持 1 小时缓存有效期与异步更新策略，降低 AI 调用成本",
                            "数据中心页面集成容量利用率仪表盘，直观展示各冰箱空间占用与平均利用率",
                            "物品管理页面新增冰箱容量进度条卡片，实时呈现已用容量与总容量占比",
                            "新增异步线程池配置（AsyncConfig），提升 AI 计算等耗时任务的处理性能",
                            "前端新增首次加载友好提示，避免用户因 AI 计算等待而重复刷新",
                            "关于页面更新日志与系统信息展示优化"
                    ))
                    .build(),
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
                    .isMajor(true)
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
                    .value("MIT License")
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
}
