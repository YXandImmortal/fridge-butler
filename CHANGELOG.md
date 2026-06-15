# Changelog

本文件记录 `fridge-butler` 项目的详细技术变更，面向开发者与贡献者。面向用户的精简版本请见 `SystemController.java` 中的 `SYSTEM_UPDATES`。

---

## [Unreleased / 0.5.0-part-1] - 2026-06-15

> 本次更新为 **release 0.5.0 的第一部分**，聚焦“游戏性功能的初步引入”，尚未包含 0.5.0 的完整内容。后续还将继续完善游戏化细节与相关测试。
> 项目根目录及前后端目录下的 `.md` 方案参考文件（如 `gamification-plan.md`、`gamification-backend-plan.md`、`gamification-frontend-plan.md` 等）未纳入 Git 管理，仅作为设计参考。

### 新增

#### 后端
- 引入游戏化系统（Gamification）核心模块：
  - 经验值（Exp）系统：记录用户经验获取、等级计算与经验日志。
  - 徽章（Badge）系统：定义徽章编码、触发类型及用户徽章发放逻辑。
  - 连续打卡（Streak）系统：支持连续使用天数统计与每日检查任务。
  - 新鲜度评分（Freshness Score）系统：每日快照冰箱食材新鲜度并生成评分。
  - 月度报告（Monthly Report）系统：按月汇总用户食材管理数据并生成报告。
  - 成就配置（Achievement Setting）与结算服务：统一配置行为奖励规则并触发结算。
- 新增定时任务：
  - `FreshnessSnapshotJob`：每日生成新鲜度快照。
  - `MonthlyReportJob`：每月生成用户月度报告。
  - `StreakDailyCheckJob`：每日检查并更新用户连续打卡状态。
- 新增 `GamificationController` 提供游戏化相关 API。
- 新增 `UserContextUtil` 工具类，便于在业务层获取当前登录用户上下文。
- 新增 `FridgeCreateResultVO`、`ItemCreateVO`、`ItemTakeOutResultVO`、`ItemUpdateResultVO` 等结果 VO，用于前后端数据交互。

#### 前端
- 新增游戏化相关页面与组件：
  - 用户中心扩展：展示等级徽章、经验进度、连续打卡、新鲜度评分、月度报告、成就面板等。
  - 新增 `gamification` 组件集：`AchievementPanel`、`BadgeSection`、`BadgeToast`、`ExpLogSection`、`ExpProgressBar`、`ExpToast`、`FreshnessHeatmap`、`FreshnessScoreCard`、`LevelBadge`、`LevelExpCard`、`LevelUpNotify`、`MonthlyReportSection`、`StreakCard` 等。
  - 新增用户相关弹窗组件：`BindEmailDialog`、`ChangeAvatarDialog`。
  - 新增 `SvgIcon` 通用图标组件。
- 新增游戏化状态管理 `stores/gamification.js` 与 API 层 `api/gamification.js`。
- 新增游戏化通知工具：`badgeNotify.js`、`expNotify.js`、`gamificationNotify.js`、`gamificationToastContainer.js`、`levelUpNotify.js`。
- 新增类型定义 `types/gamification.js`。
- 新增样式文件：`gamification.scss`、`svg-icon.scss`。
- 扩展 iconfont 字体资源以支持新图标。

#### 集成与配置
- 在 `SecurityConfig` 中开放/适配游戏化相关接口权限。
- 在冰箱、物品、用户、AI 聊天、数据中心等既有业务中接入经验获取与徽章触发逻辑。
- 数据库脚本 `fridge-butler-database.sql` 同步更新以支持新增的游戏化表结构。
- `application.yaml` 增加相关配置项。

### 优化
- 优化既有 VO 与部分服务实现，以配合游戏化数据的返回与展示。
- 调整前端多处视图（登录、注册、冰箱列表/详情、物品管理、数据中心、超级管理员用户管理等）以集成游戏化元素或适配新接口。
- 更新 `components.d.ts` 与 `main.js` 以注册新增组件与全局配置。

---

## [release 0.4.1] - 2026-6-10

### 新增
- **前端自定义 UI 组件体系完善**
  - 新增 `CustomInput.vue`：通用输入框组件，完整替代 `EnhancedInput`，支持 `text` / `password` / `textarea` 三种模式，支持前缀图标、清空按钮、密码显隐切换、字数限制、错误状态、聚焦动画与 `autocomplete` 属性
  - 新增 `CustomInputNumber.vue`：数字输入框组件，支持 `min` / `max` / `step` / `precision`，支持 `size` 与 `width` 配置，替代 `el-input-number` 与 `el-slider`
  - 新增 `CustomDatePicker.vue`：日期选择器组件，支持单日期与范围选择，内置日历视图、年月切换、空状态与清除功能，替代 `el-date-picker`
  - 新增 `CustomSwitch.vue`：开关组件，支持 `activeValue` / `inactiveValue`、前置/后置文本标签、`inline-prompt` 模式、加载状态与键盘无障碍操作，替代 `el-switch`
  - 新增 `CustomCheckbox.vue`：复选框组件，支持标签文本与禁用状态，替代 `el-checkbox`
  - `CustomButton.vue` 增强：新增 `round`（全圆角）与 `plain`（朴素/描边）属性，支持三种类型的 plain 变体；字体权重由 200 调整为 400，行高统一为 16px；新增 `focus-visible` 轮廓光环；大尺寸与小尺寸 padding 优化
  - `CustomSelect.vue` 增强：新增 `size`（large/default/small）与 `variant`（default/search）属性；下拉面板支持空状态插槽与默认空提示文本；选项选中图标逻辑优化

### 优化
- **Element Plus 基础组件全面替换为自定义组件**
  - 认证相关页面（登录、注册、激活、忘记密码）：`EnhancedInput` / `el-input` 全面替换为 `CustomInput`，`el-checkbox` 替换为 `CustomCheckbox`，统一使用 `size="large"` 与 `autocomplete` 属性
  - 物品详情/编辑弹窗：`EnhancedInput` 替换为 `CustomInput`，`el-input-number` 替换为 `CustomInputNumber`，`el-date-picker` 替换为 `CustomDatePicker`，`el-slider`（保质期滑块）替换为 `CustomInputNumber`，输入体验更精准
  - AI 向导（冰箱创建、物品创建）：`EnhancedInput` 替换为 `CustomInput`，`el-slider` 替换为 `CustomInputNumber`，`el-switch` 替换为 `CustomSwitch`
  - 系统配置页面：`EnhancedInput` 替换为 `CustomInput`，`el-switch` 替换为 `CustomSwitch`
  - 邮箱验证码输入：`EnhancedInput` 替换为 `CustomInput`
  - 落地页 CTA 按钮：`el-button` 替换为 `CustomButton`
  - 登录注册按钮文案优化：「登 录」→「点击登录」，「注 册」→「没有账号？注册」
- **验证码主题适配**：后端 `CaptchaController` 接口新增 `theme` 查询参数（`light` / `dark`），根据主题返回对应背景色验证码；前端 `CaptchaInput.vue` 监听主题变化自动刷新验证码；验证码输出格式由 GIF 升级为 PNG，高度由 44 调整为 42，视觉更统一
- **后端 VO 包结构重构**：所有 VO 类由扁平的 `com.yx.fridgebutler.vo` 包迁移至按业务模块分包的子包：`auth`、`category`、`fridge`、`item`、`system`、`unit`、`unit/type`、`user`，共 17 个文件移动，架构更清晰，import 路径同步更新
- **冰箱列表页增强**：已停用冰箱支持专属卡片样式（虚线边框、银灰渐变背景、「已停用」角标），状态一目了然
- **样式系统优化**：`element-overrides.scss` 表单验证错误样式适配自定义组件；`global.scss` 新增阴影变量与工具类；多处表单错误聚焦阴影统一为品牌色/错误色光环

### 工程
- **版本号统一升级**：后端 `pom.xml`、前端 `package.json` 同步升级至 `release 0.4.1`
- **构建时间更新**：`application.yaml` 构建时间更新为 `20260610-02`
- **组件类型声明更新**：`components.d.ts` 移除 `EnhancedInput`，新增 4 个自定义组件声明

---

## [release 0.4.0] - 2026-6-8

### 新增
- **邮箱安全体系**
  - 完整邮箱模块：验证码发送与校验、找回密码、重置密码、绑定邮箱
  - `EmailCaptchaManager`：统一管理邮箱验证码生命周期（60秒冷却、每日上限10条、30分钟过期）
  - `EmailTemplate` 枚举：支持注册验证、密码重置、密码修改、绑定邮箱等场景模板
  - `EmailService` / `EmailServiceImpl`：基于 Spring `JavaMailSender` 的邮件发送服务，支持异步发送与 HTML 模板渲染
  - 前端 `EmailVerifyInput.vue`：通用邮箱验证码输入组件，支持发送冷却倒计时与校验提示
  - 前端 `ForgotPasswordView.vue`：完整的找回密码页面，支持邮箱验证、密码重置与自动登录
  - `application-dev.yaml` / `application-prod.yaml` 新增邮件服务器配置（SMTP、用户名、密码等）
- **通知系统全面升级**
  - 新增 `BIND_EMAIL_REMINDER` 通知类型：未绑定邮箱用户登录时自动创建提醒（当天仅一次，避免过度打扰）
  - 新增 `SYSTEM` 通知类型：覆盖管理员重置密码、密码重置成功、新用户欢迎、密码修改成功、邮箱绑定成功等安全事件
  - 通知摘要 `NotificationSummaryVO` 扩展：新增 `bindEmailReminderCount` 与 `systemNotificationCount`
  - 通知仓库新增批量标记已读方法：`markAsReadByIds`、`markAsReadByUserIdAndType`
  - 通知仓库新增存在性查询：`existsByUserIdAndTypeAndCreateTimeGreaterThanEqual`（用于当天去重）
  - 重要通知获取逻辑优化：获取最新一条时自动将其他未读重要通知标记为已读，避免连续弹窗打扰
  - 前端 `ImportantNoticeDialog.vue`：重要通知详情弹窗组件，支持展示完整内容与确认
  - 前端消息中心支持快捷操作按钮：「去绑定」跳转个人中心邮箱面板、「去完成指引」触发全局快速引导
  - 前端 Header 通知下拉面板支持 `BIND_EMAIL` 与 `VIEW_GUIDE` 操作按钮
  - 前端通知 Store `getActionRoute` 扩展：支持 `BIND_EMAIL` 与 `VIEW_GUIDE` 动作类型路由
- **用户引导增强**
  - `TourStore` 新增 `pendingQuickGuide` 状态与 `requestQuickGuide()` 方法，支持跨组件触发全局快速指引
  - `MainLayout.vue` 监听 `pendingQuickGuide` 变化，收到请求后自动调用 `startTourWithRouteCheck()`
  - 用户中心支持通过 URL Query `edit=email` 自动打开邮箱绑定面板
- **主题切换动画**
  - 明暗主题切换新增圆形扩散动画效果，视觉过渡更流畅自然

### 优化
- **注册流程**：`RegisterRequest.emailCaptcha` 校验规则由 `@Size(min=6, max=6)` 优化为 `@Pattern(regexp = "^$|^\\d{6}$")`，允许未填邮箱时空值通过，填写时强制6位数字
- **消息中心界面重构**：列表高度调整为 700px，卡片悬停动画由 Y 轴位移改为 X 轴位移，操作按钮区与消息卡片分离，布局更紧凑
- **通知类型标签样式**：`BIND_EMAIL_REMINDER` 类型使用 `primary` 标签样式，视觉区分更明确
- **前端 `.gitignore`**：新增 `public/maintenance-config.php` 忽略规则

### 修复
- **管理员后台看板日志**：修复耗时字段名错误导致数值不显示的问题
- **AI 每日小贴士**：修复 AI 生成空内容导致展示异常的问题

### 工程
- **版本号统一升级**：后端 `pom.xml`、前端 `package.json` 同步升级至 `release 0.4.0`
- **构建时间更新**：`application.yaml` 构建时间更新为 `20260608-03`
- **系统版本号**：`application.yaml` 与 `SystemController` 同步更新为 `release 0.4.0`

---

## [release 0.3.1] - 2026-6-5

### 新增
- **激活码生命周期增强**：新增 `ISSUED`（已发放）状态，支持管理员先发放密钥、用户再绑定激活的完整流程；`verifyKey` 接口由仅返回 Token 升级为返回完整 `LoginVO`，激活后自动完成登录，无需二次请求。
- **账号禁用专属页面**：前端新增 `AccountDisabledView`，账号被禁用时展示友好提示与管理员联系方式，替代原有全局错误弹窗，体验更体面。
- **管理员重置密码结果弹窗**：新增 `ResetPasswordResultDialog`，重置密码后清晰展示临时密码与一键复制入口，管理体验更顺畅。
- **业务系统默认数据初始化扩展**：`BizSystemDataInitializer` 新增 8 种冰箱类型自动初始化，首次部署数据更完整。

### 优化
- **后端接口统一 `/api` 上下文路径**：所有后端接口增加 `/api` 前缀，前后端配置同步适配，部署更规范，反向代理规则更清晰。
- **HTTP 状态码与业务码语义对齐**：`ResultCode` 枚举全面绑定 `HttpStatus`，`GlobalExceptionHandler` 根据业务异常返回对应 HTTP 状态码（401/403/404/503 等），前端可精准识别错误类型并作出差异化处理。
- **空白手机号唯一键冲突修复**：注册与用户信息修改时，空白手机号统一转为 `null`，避免空字符串触发数据库唯一键冲突。
- **前端 HTTP 错误处理全面增强**：请求拦截器适配 401/403/404/503/400 全场景，登录失败、账号禁用、资源不存在、AI 服务繁忙等场景给出精准提示与页面跳转。
- **生产环境头像 404 修复**：6 款主题 SVG 头像迁移至 `frontend/public/avatars/`，解决生产环境构建后头像资源路径失效问题。
- **超级管理员屏蔽通知中心**：Header 通知图标与消息轮询对超级管理员隐藏，避免无意义请求与视觉干扰。
- **管理员后台激活码管理升级**：列表支持 `ISSUED` 状态筛选与展示，新增「发放」操作按钮，密钥生命周期管理更完善。
- **UI 细节优化**：移除部分表格列的 `show-overflow-tooltip`，数据展示更清爽；`ChangePasswordDialog` 表单验证流程优化。

### 工程
- **生产环境数据库连接优化**：关闭 MySQL SSL，启用 `allowPublicKeyRetrieval`，适配本地与内网部署场景。
- **版本号统一升级**：后端 `pom.xml`、前端 `package.json` 同步升级至 release 0.3.1，构建时间更新为 `20260605-02`。

---

## [release 0.3.0] - 2026-6-4

### 新增
- **超级管理员后台**：完整的后台管理模块，包含仪表盘数据概览、用户管理、操作日志审计、系统配置管理、重要通知广播与激活码管理六大功能板块。
- **激活码系统**：支持生成、分发与管理用户激活密钥，控制普通用户的注册准入，适配内测与邀请制场景。
- **系统动态配置**：基于 `sys_config` 表的 key-value 配置体系，支持系统公告、注册开关、密钥激活要求等配置项的热更新，无需重启服务。
- **操作日志审计**：`LogInterceptor` 拦截器增强，支持 HTTP 请求信息异步持久化至 `sys_oper_log` 表，实现全链路追踪与行为审计。
- **重要通知广播**：管理员可向全部用户一键广播重要通知，支持 5 分钟幂等校验；新注册用户自动同步接收历史最新通知。
- **官网落地页（Landing Page）**：项目开源官方网站上线，展示产品亮点、核心功能与内测申请入口。
- **首次登录强制改密**：新增 `password_updated_at` 字段，管理员创建/重置密码后，用户首次登录强制修改初始密码，提升账号安全。
- **最后登录时间记录**：`SysUser` 新增 `last_login_time` 字段，支持追踪用户活跃情况。
- **JWT Token 增强**：Payload 新增 `activated` 声明，支持前端根据 Token 判断用户激活状态并控制功能访问。
- **多环境配置支持**：新增 `application-dev.yaml` 与 `application-prod.yaml`，开发/生产环境配置隔离，部署更灵活。
- **管理员账号自动初始化**：新增 `AdminAccountInitializer`，首次部署时可自动创建超级管理员账号，支持通过环境变量控制开关与凭据输出。
- **系统角色自动初始化**：新增 `SysRoleInitializer`，首次部署时自动初始化预设角色数据。
- **业务系统默认数据初始化**：新增 `BizSystemDataInitializer`，支持冰箱类型等基础数据自动初始化。
- **前端 Markdown 编辑器**：新增 `md-editor-v3` 依赖，管理员后台支持富文本内容编辑。

### 优化
- **注册流程体验升级**：普通用户注册成功后直接返回 JWT Token，前端无需二次调用登录接口即可进入系统。
- **用户首页全面增强**：数据卡片布局优化，功能入口更直观，欢迎体验更佳。
- **前端组件目录重构**：按功能域重新组织组件目录结构（`brand/`、`form/`、`layout/`、`ui/`、`tour/` 等），可维护性大幅提升。
- **CORS 跨域配置增强**：支持通过 `cors.allowed-origins` 配置多域名，适配多环境部署需求。
- **默认头像更新**：用户默认头像由 `bot` 更新为 `ice`，更贴合产品主题。
- **业务异常信息细化**：多处业务异常提示优化，错误信息更精准友好。
- **通知中心增强**：新增「重要通知」类型统计与展示，支持管理员广播消息的分类查看。
- **前端路由重构**：超级管理员路由升级为多页面后台（仪表盘、用户、日志、系统、通知），`/` 根路径改为落地页。

### 工程
- **项目正式开源**：添加 `LICENSE`（AGPL-3.0），完善 `README.md`，`pom.xml` 补充开源协议、开发者与 SCM 信息，仓库即将公开。
- **版本号统一升级**：后端 `pom.xml` 与前端 `package.json` 同步升级至 release 0.3.0，构建时间更新为 20260604-01。
- **后端服务端口调整**：默认端口由 8080 调整为 8081，避免常见端口冲突。
- **数据库脚本全面更新**：补充管理员后台、激活码、系统配置、操作日志、重要通知等新增表结构。
- **前端依赖升级**：新增 `md-editor-v3` ^6.5.1，`package-lock.json` 同步更新。

---

## [beta 0.2.1] - 2026-5-27

### 新增
- **用户引导（Tour）系统**：基于 Element Plus Tour 实现，首次登录自动播放全局布局引导，覆盖首页、冰箱列表、冰箱详情、物品管理、分类管理、单位管理、数据中心、通知中心、个人中心、关于页面等核心模块的分步教程。
- 后端用户表新增 `guide_completed` 字段，支持记录用户是否完成新手指引。
- 新增 `PATCH /user/guide-complete` 接口，用于标记当前登录用户的新手指引已完成。
- `UpdateLogVO` 新增 `summary` 字段，支持版本一句话摘要展示。
- 关于页面支持链接类型信息渲染（URL 可新标签页打开、Email 支持 mailto 跳转）。

### 优化
- 头像资源全面焕新：新增 `egg`、`ice`、`leaf`、`milk`、`orange`、`snowflake` 6 款主题 SVG 头像，默认头像更新为 `egg`；旧头像归档至 `backup` 目录。
- Header 组件新增快速上手指南按钮，支持随时唤起引导流程。
- Element Plus Tour 组件全局主题覆盖：适配浅色/深色模式，统一弹窗圆角、玻璃态背景、按钮风格与步骤指示器样式。
- Logo 组件支持点击跳转至用户首页，提升导航便捷性。
- 关于页面更新日志支持 `summary` 字段展示，信息层级更清晰。
- 多处代码格式规范化与模板标签自闭合优化。

### 工程
- 前端依赖升级：`axios` ^1.16.1、`echarts` ^6.1.0、`element-plus` ^2.14.0、`vue-router` ^5.0.7。
- 前后端版本号与构建时间同步升级至 beta 0.2.1 / 20260527-03。

---

## [beta 0.2.0] - 2026-5-26

### 新增
- **AI 热量计算功能**：支持基于引用物品/冰箱计算总热量，也可直接询问菜品热量，AI 自动估算并给出营养成分分析。
- **AI 物品创建向导**：用户可通过 AI 聊天以引导式交互添加物品，支持名称、分类、数量单位、保质期、备注、确认 6 步流程。
- 后端新增 `calorie_calculation` 意图识别与结构化数据处理：总热量、食材明细、营养成分、份量说明与 AI 总结。
- 后端新增 `item_creation_wizard` 意图识别与纯后端状态驱动推进机制，无需 LLM 参与向导流程，响应更稳定。
- 前端新增 `ItemCreationWizard` 组件：步骤条、表单摘要、多种输入类型（文本/选择/组合单位/组合日期/文本域）与确认卡片。
- 物品管理页新增「AI 帮我添加」按钮接入向导，点击自动跳转 AI 聊天并携带当前冰箱 ID 触发创建流程。
- 前端新增热量计算卡片 UI：渐变总热量头部、食材明细列表、蛋白质/碳水/脂肪营养标签与 AI 总结。
- `ChatStructuredMessage` 全面支持 `item_creation_wizard` 与 `calorie_calculation` 消息类型渲染。

### 优化
- 关于页面更新日志支持重大更新标识：`isMajor` 字段驱动金色时间轴高亮与「重大更新」徽章展示。
- 系统关于信息卡片支持图标展示，视觉层次更丰富。
- 全局图标库扩充：新增物品、编辑框、保存、退出、勾选等图标，按钮与操作区视觉统一。
- 物品管理页取出操作交互优化：下拉菜单升级为 Popover 浮层，避免层级遮挡与误触。
- 多处按钮组件统一图标前缀与样式规范，移除重复 CSS，维护性提升。

### 工程
- AI 聊天请求新增 `fridgeId` 字段，支持从物品管理页跳转时自动关联目标冰箱。
- 意图识别提示词模板热更新支持 `calorie_calculation` 与 `item_creation_wizard` 两种新意图。
- 前后端版本号同步升级至 beta 0.2.0。

---

## [beta 0.1.6] - 2026-5-24

### 新增
- **AI 冰箱创建向导**：用户可通过 AI 聊天以引导式交互创建冰箱，支持名称、类型、容量、默认设置、地址与备注 6 步流程。
- 后端新增 `fridge_creation_wizard` 意图识别与纯后端状态驱动推进机制，无需 LLM 参与向导流程，响应更稳定。
- 前端新增 `FridgeCreationWizard` 组件：步骤条、表单摘要、多种输入类型（文本/选择/滑块/开关/组合输入）与确认卡片。
- 冰箱列表页「AI 帮我创建」按钮接入向导，点击自动跳转 AI 聊天并触发创建流程。
- 新增 `PromptTemplateLoader` 提示词模板加载器：从 `classpath:prompts/*.md` 加载 AI 提示词，支持 60 秒定时热刷新。
- AI 意图识别、通用对话、菜谱推荐、容量估算、每日小贴士等提示词全面模板化，修改无需重启服务。
- 新增 `AiResponseUtils` 工具类：统一处理模型响应清洗与 JSON 子串提取，提升 AI 响应解析鲁棒性。
- DeepSeek 服务接口增强：新增 `chat(DeepSeekChatRequest)` 与 `chatStream(DeepSeekChatRequest)`，支持自定义 temperature 与 responseFormat。

### 优化
- AI 聊天历史新增滑动窗口截断策略：单会话默认保留最近 10 轮对话，避免上下文过长导致 Token 消耗激增。
- AI 附件上下文增强：用户引用冰箱时，AI 可感知已过期/临期/一般/新鲜/长保质期分类统计与具体物品明细。
- 前端暗色模式下冰箱类型 SVG 图标自动反色，确保视觉可见性。
- Element Plus 表单验证错误 focus 样式统一提取至全局覆盖，移除组件级重复代码。
- 物品管理页容量卡片显示逻辑优化：改为展示基于利用率的已用容量（升），数据更准确。
- 数据中心分类聚合逻辑修正：按物品种类计数替代数量累加，避免同一分类多数量物品失真。

### 工程
- 数据库初始化脚本全面补充：涵盖 AI 聊天会话/消息、冰箱容量利用率缓存、冰箱类型、通知、每日小贴士等全部新增表。
- 前后端版本号同步升级至 beta 0.1.6。

---

## [beta 0.1.5] - 2026-5-23

### 新增
- **消息通知系统**：支持临期/过期自动提醒、容量预警与系统通知，消息中心集中管理。
- 后端新增完整消息通知模块：实体、枚举、Repository、Service、Controller 与定时任务。
- 新增 `NotificationGenerateJob` 定时任务，每天凌晨 2:00 自动生成临期/过期消息提醒，自动去重。
- 前端新增消息中心页面，支持按类型筛选、未读统计、一键已读与消息跳转。
- Header 通知图标升级为下拉面板，实时展示最近未读消息，支持快捷标记已读与跳转。
- 前端新增通知 Store，支持 30 秒轮询未读数，消息状态实时同步。

### 优化
- AI 聊天支持引用附件上下文：用户可引用冰箱或物品，AI 回答时关联实时业务数据。
- AI 菜谱推荐增强：优先基于用户指定的食材推荐，尊重用户数量要求，全量库存作为补充参考。
- AI 聊天空消息场景优化：仅携带附件时自动补充默认提示语。
- AI 消息历史支持附件快照还原，跨会话上下文完整性提升。
- 物品新增记录实体新增 `remark` 备注字段，支持记录补充信息。

### 工程
- 前后端版本号同步升级至 beta 0.1.5。

---

## [beta 0.1.4] - 2026-5-21

### 新增
- **冰箱类型系统**：支持单门、双门、三门、对开门、十字对开门等常见冰箱类型，创建与搜索时可选。
- 冰箱创建弹窗、详情页编辑与列表搜索全面集成冰箱类型选择，支持类型图标可视化展示。

### 优化
- AI 容量估算增强：估算时关联冰箱类型与备注信息，提升空间利用率估算准确度。
- 容量统计缓存策略升级：冰箱类型变更将触发容量利用率重新计算，确保数据实时性。
- 物品管理新增新鲜度排序：按 r 值（剩余天数/保质期×100）排序，直观查看食材新鲜程度。
- 前端 `CustomSelect` 组件增强，支持 prefix 插槽与更灵活的选项展示。
- 冰箱列表页新增搜索重置按钮，筛选操作更便捷。
- 临期物品表、物品分类与单位管理页面交互细节优化。

### 工程
- 前后端版本号同步升级至 beta 0.1.4。

---

## [beta 0.1.3] - 2026-5-21

### 新增
- **每日小贴士功能**：由 DeepSeek AI 每日自动生成冰箱相关的冷知识、实用技巧、冷笑话与谜语。
- 后端新增每日小贴士完整模块：实体、枚举、Repository、Service、Controller 与定时任务。
- 新增 `/daily-tip/today` 与 `/daily-tip/by-date` 接口，支持匿名访问，无数据时实时调用 AI 生成并持久化。
- 新增 `DailyTipGenerateJob` 定时任务，每天凌晨 1:00 预生成当日小贴士，避免首次访问等待。
- 前端侧边栏集成每日小贴士卡片，支持类型标签、emoji、谜语交互式答案与本地缓存。
- 前端新增 `dailyTip.js` API 模块与 `DAILY_TIP_API.md` 接口文档。

### 优化
- AI 聊天用户消息气泡背景色优化，提升视觉辨识度。
- 用户首页欢迎区与消息时间样式微调，整体排版更紧凑。
- Sidebar 布局重构，适配底部固定小贴士区域，导航区独立滚动。

---

## [beta 0.1.2] - 2026-5-20

### 新增
- **AI 聊天 SSE 流式输出**：基于 `SseEmitter` 分阶段推送 text/card/done 事件，实现逐字打字机效果。
- 后端新增 `/ai/chat/stream` 流式接口。
- DeepSeek 服务接入流式对话 API（`chatStream`），支持逐字符实时获取模型回复。
- 前端新增 `AiMessageContent` 组件，封装 Markdown 解析与 `Highlight.js` 高亮。

### 优化
- AI 聊天支持中断请求（AbortController），用户可随时终止当前对话。
- 物品管理页冰箱容量卡片新增数据刷新频率提示，交互更友好。
- 安全配置优化：放行 ASYNC/ERROR 分发类型，确保 SSE 异步请求正常处理。
- 后端代码现代化：`IntentResult` 改为 record 类型，`Math.clamp` 替代手动边界计算。

### 工程
- 新增 SSE 使用指南文档（`SSE_USAGE_GUIDE.md`），前后端各一份。

---

## [beta 0.1.1] - 2026-5-19

### 新增
- **冰箱容量利用率统计**：基于 DeepSeek AI 大模型智能估算各冰箱空间占用率。
- 后端新增容量统计服务与缓存机制，支持 1 小时缓存有效期与异步更新策略，降低 AI 调用成本。
- 数据中心页面集成容量利用率仪表盘，直观展示各冰箱空间占用与平均利用率。
- 物品管理页面新增冰箱容量进度条卡片，实时呈现已用容量与总容量占比。
- 新增异步线程池配置（`AsyncConfig`），提升 AI 计算等耗时任务的处理性能。

### 优化
- 前端新增首次加载友好提示，避免用户因 AI 计算等待而重复刷新。
- 关于页面更新日志与系统信息展示优化。

---

## [beta 0.1.0] - 2026-5-19

### 新增
- **AI 智能冰箱管家**：集成 DeepSeek 大模型，支持自然语言交互与意图识别。
- 用户首页全面重构，新增个性化欢迎语、数据统计卡片与 AI 聊天助手入口。
- AI 聊天支持会话管理、历史消息回溯与结构化数据渲染（冰箱/物品/临期提醒）。
- 新增临期/过期物品统计摘要接口，首页实时展示食材保鲜状态。

### 工程
- 后端 DTO 包结构按业务模块重构（auth、fridge、item、category 等），架构更清晰。
- 安全配置升级：数据库密码、JWT 密钥等敏感信息强制环境变量注入，提升安全性。
- 关于页面更新日志升级为 Timeline 时间轴展示，视觉体验更佳。
- 系统版本正式进入 beta 阶段。

---

## [alpha 0.0.9] - 2026-5-17

### 优化
- 冰箱创建、物品分类创建、物品单位创建改为弹窗形式，交互更流畅。
- 优化 `InputDialog` 组件，支持无数据创建场景。
- 数据中心图表展示优化，数据呈现更精准。
- 后端系统配置改为动态注入，架构更灵活。
- 扩充系统图标库，丰富界面表现力。

---

## [alpha 0.0.8] - 2026-5-9

### 新增
- 完善冰箱物品管理功能，支持全部物品管理操作。
- 新增物品分类管理功能，自由创建物品分类。
- 新增物品单位管理功能，物品数量一目了然。

---

## [alpha 0.0.7] - 2026-4-30

### 优化
- 全面优化浅色与深色主题，界面更美观。
- 物品管理功能上线，支持浏览与增加物品。

---

## [alpha 0.0.6] - 2026-04-20

### 新增
- 新增系统颜色主题切换功能，预设浅色与深色主题。
- 新增关于系统页面，介绍系统信息与功能。

### 优化
- 优化冰箱详情页交互体验。
- 修复若干已知问题，提升系统稳定性。

---

## [alpha 0.0.5] - 2026-04-15

### 新增
- 新增冰箱搜索功能，支持多条件筛选。
- 支持用户头像上传与修改。

### 优化
- 优化系统性能，提升页面加载速度。

---

## [alpha 0.0.4] - 2026-04-10

### 新增
- 新增用户个人中心页面。
- 支持修改密码功能。

### 优化
- 优化登录页面视觉设计。
