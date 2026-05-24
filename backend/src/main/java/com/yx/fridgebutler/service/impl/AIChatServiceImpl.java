package com.yx.fridgebutler.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yx.fridgebutler.dto.aichat.AIChatAttachment;
import com.yx.fridgebutler.dto.aichat.AIChatHistoryMessage;
import com.yx.fridgebutler.dto.aichat.AIChatRequest;
import com.yx.fridgebutler.dto.aichat.AIChatWizardContext;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatMessage;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatRequest;
import com.yx.fridgebutler.dto.item.ItemSearchRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.yx.fridgebutler.entity.AiChatMessage;
import com.yx.fridgebutler.entity.AiChatSession;
import com.yx.fridgebutler.entity.BizFridge;
import com.yx.fridgebutler.entity.BizFridgeItem;
import com.yx.fridgebutler.entity.BizItemUnit;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.AiChatMessageRepository;
import com.yx.fridgebutler.repository.AiChatSessionRepository;
import com.yx.fridgebutler.repository.BizFridgeItemRepository;
import com.yx.fridgebutler.repository.BizFridgeRepository;
import com.yx.fridgebutler.repository.BizItemUnitRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.config.PromptTemplateLoader;
import com.yx.fridgebutler.service.AIChatService;
import com.yx.fridgebutler.service.DeepSeekService;
import com.yx.fridgebutler.util.AiResponseUtils;
import com.yx.fridgebutler.service.FridgeService;
import com.yx.fridgebutler.service.ItemService;
import com.yx.fridgebutler.vo.FridgeVO;
import com.yx.fridgebutler.vo.ItemVO;
import com.yx.fridgebutler.vo.TakeOutDailyStatisticsVO;
import com.yx.fridgebutler.vo.aichat.AIChatDataVO;
import com.yx.fridgebutler.vo.aichat.AIChatMessageVO;
import com.yx.fridgebutler.vo.aichat.AIChatReplyVO;
import com.yx.fridgebutler.vo.aichat.AIChatSessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * AI 聊天服务实现类。
 * <p>核心流程：意图识别 → 业务数据获取 → 结构化响应组装。</p>
 */
@Slf4j
@Service
public class AIChatServiceImpl implements AIChatService {

    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter CHART_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd");

    private static final String INTENT_SYSTEM_PROMPT = """
            你是一个冰箱管理助手的意图识别系统。请严格分析用户输入与用户引用的上下文，返回纯JSON，不要包含任何其他文字（包括markdown代码块标记、解释说明等）。

            用户引用的上下文是用户主动指定的重点对象，分析意图时必须优先考虑用户引用的内容，不能忽略。

            支持的意图类型：
            - fridge_list: 查看冰箱列表，如"我的冰箱有哪些"
            - item_list: 查看物品/库存/食材列表，可能包含关键词和冰箱名称，如"冰箱里还有什么鸡蛋"
            - expiring_alert: 查看临期/过期提醒，如"有什么快过期的"
            - recipe_recommend: 根据用户描述或用户引用的食材推荐菜谱，如"今天吃什么""用西冷牛排做什么菜"
            - trend_chart: 查看趋势/统计图表，如"近7天取出趋势"
            - action_confirm: 删除/清空/移除等需要确认的操作，如"删除厨房冰箱"
            - fridge_creation_wizard: 用户想要创建新冰箱，如"帮我创建一个冰箱""我要新建冰箱""添加一个冰箱"
            - text: 通用对话、问候、闲聊、无法识别的意图

            返回格式（严格JSON，不要换行符外的其他格式）：
            {"intent":"意图类型","params":{...},"confidence":0.95}

            参数说明：
            - fridge_list: 无参数，params为空对象{}
            - item_list: {"keyword":"搜索关键词（如'鸡蛋'），没有则null","fridgeName":"冰箱名称（如'厨房冰箱'），没有则null"}
            - expiring_alert: 无参数，params为空对象{}
            - recipe_recommend: 无参数，params为空对象{}
            - trend_chart: {"type":"take_out|add|both","days":7或30}
            - action_confirm: {"action":"delete_fridge|clear_expired|...","targetName":"目标名称"}
            - fridge_creation_wizard: 无参数，params为空对象{}
            - text: 无参数，params为空对象{}

            示例：
            用户输入："我的冰箱有哪些"
            输出：{"intent":"fridge_list","params":{},"confidence":0.99}

            用户输入："厨房里还有什么鸡蛋"
            输出：{"intent":"item_list","params":{"keyword":"鸡蛋","fridgeName":"厨房冰箱"},"confidence":0.98}

            用户输入："今天吃什么"
            输出：{"intent":"recipe_recommend","params":{},"confidence":0.97}

            用户输入："删除厨房冰箱"
            输出：{"intent":"action_confirm","params":{"action":"delete_fridge","targetName":"厨房冰箱"},"confidence":0.96}

            用户输入："把鸡蛋删了"
            输出：{"intent":"action_confirm","params":{"action":"delete_item","targetName":"鸡蛋"},"confidence":0.92}

            用户输入："帮我创建一个冰箱"
            输出：{"intent":"fridge_creation_wizard","params":{},"confidence":0.98}

            用户输入："今天天气怎么样"
            输出：{"intent":"text","params":{},"confidence":0.95}

            注意事项：
            1. 如果用户输入与冰箱管理完全无关（如"今天天气怎么样"），返回 text
            2. 如果用户意图不明确或含糊，返回 text
            3. 必须只返回JSON字符串，不要添加```json标记
            """;

    private static final String RECIPE_SYSTEM_PROMPT = """
            你是一位擅长家常菜的厨师。请根据用户的需求推荐合适的菜谱。

            要求：
            1. 如果用户指定了特定食材，优先基于这些食材推荐菜谱，不要把全量库存当作唯一依据
            2. 如果用户没有指定食材，基于冰箱整体库存推荐
            3. 尊重用户要求的数量（如用户说"两个"就推荐两道，不要说2-3道）
            4. 每道菜包含：名称、难度（简单/中等/困难）、预计烹饪时间、已匹配的食材列表、缺少的食材列表（如有）、简短描述
            5. 返回严格JSON格式，不要包含任何其他文字（包括markdown代码块标记）：

            {"recipes":[{"name":"菜名","difficulty":"简单","cookTime":"10分钟","matchedItems":["食材1","食材2"],"missingItems":["食材3"],"description":"描述"}],"text":"根据你的需求，为你推荐以下x道菜："}
            """;

    /**
     * 通用文本对话的系统提示词。
     * <p>定义了助手的角色定位、能力边界和回答规范。</p>
     */
    private static final String GENERAL_CHAT_SYSTEM_PROMPT = """
            你是"冰箱管家"，一位专注于冰箱食材管理的智能助手。

            【你的能力】
            1. 回答用户关于冰箱库存、食材保质期、冰箱管理的提问
            2. 根据用户引用的食材或冰箱数据给出建议
            3. 进行友好、简短的日常对话

            【回答规范】
            - 回答要口语化、简洁，控制在150字以内
            - 当用户引用了具体食材或冰箱时，优先针对引用内容回答，不要泛泛而谈
            - 不要编造用户冰箱中不存在的食材信息
            - 如果用户问题与冰箱管理无关，礼貌地告知并引导回正题
            """;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private FridgeService fridgeService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private BizFridgeItemRepository itemRepository;

    @Autowired
    private BizItemUnitRepository unitRepository;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private AiChatSessionRepository sessionRepository;

    @Autowired
    private AiChatMessageRepository messageRepository;

    @Autowired
    private PromptTemplateLoader promptLoader;

    @Value("${ai.chat.max-history-rounds:10}")
    private Integer maxHistoryRounds;

    /**
     * {@inheritDoc}
     * <p>
     * 处理流程（二期增加持久化）：
     * <ol>
     *   <li>查找或创建会话（支持跨页面刷新恢复历史）</li>
     *   <li>从数据库加载历史消息</li>
     *   <li>保存用户消息到数据库</li>
     *   <li>调用 DeepSeek 进行意图识别</li>
     *   <li>根据意图类型调用对应 Handler 获取业务数据</li>
     *   <li>保存 AI 回复到数据库</li>
     *   <li>更新会话最后活跃时间</li>
     *   <li>组装结构化响应并返回</li>
     * </ol>
     */
    @Override
    public AIChatDataVO chat(AIChatRequest request) {
        Long currentUserId = getCurrentUserId();
        String sessionId = request.getSessionId();
        boolean isNewSession = false;

        // 1. 查找或创建会话
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            isNewSession = true;
        }

        AiChatSession session;
        if (!isNewSession) {
            Optional<AiChatSession> existing = sessionRepository
                    .findBySessionIdAndUserIdAndIsDeleted(sessionId, currentUserId, (byte) 0);
            if (existing.isPresent()) {
                session = existing.get();
            } else {
                session = createNewSession(sessionId, currentUserId, request.getMessage());
            }
        } else {
            session = createNewSession(sessionId, currentUserId, request.getMessage());
        }

        // 2. 从数据库加载历史（优先于前端传的 history，支持跨页面刷新）
        List<AIChatHistoryMessage> dbHistory = buildHistoryFromDb(sessionId);

        // 3. 校验附件权限
        validateAttachments(request.getAttachments(), currentUserId);

        // 4. 处理空消息并构建附件上下文
        String userMessage = resolveUserMessage(request.getMessage(), request.getAttachments());
        String attachmentContext = buildAttachmentContext(request.getAttachments());

        // 5. 保存用户消息
        saveUserMessage(sessionId, userMessage, request.getAttachments());

        // === 向导流程优先处理：若前端处于冰箱创建向导中，直接后端状态驱动推进 ===
        if (request.getWizardContext() != null
                && "fridge_creation".equals(request.getWizardContext().getType())) {
            AIChatReplyVO reply = handleFridgeCreationWizard(request);
            saveAssistantMessage(sessionId, reply);
            session.setLastActiveTime(Instant.now());
            sessionRepository.save(session);
            List<String> suggestions = generateSuggestions(reply.getMessageType());
            return AIChatDataVO.builder()
                    .sessionId(sessionId)
                    .reply(reply)
                    .suggestions(suggestions)
                    .build();
        }

        // 6. 意图识别（使用数据库历史）
        IntentResult intent = recognizeIntent(userMessage, dbHistory, attachmentContext);
        log.info("AI 意图识别结果：intent={}, params={}, confidence={}",
                intent.intent, intent.params, intent.confidence);

        // 5. 根据意图分发处理
        AIChatReplyVO reply;
        try {
            reply = switch (intent.intent) {
                case "fridge_list" -> handleFridgeList();
                case "item_list" -> handleItemList(intent.params);
                case "expiring_alert" -> handleExpiringAlert();
                case "recipe_recommend" -> handleRecipeRecommend(userMessage, request.getAttachments());
                case "trend_chart" -> handleTrendChart(intent.params);
                case "action_confirm" -> handleActionConfirm(intent.params);
                case "fridge_creation_wizard" -> handleFridgeCreationWizardInit();
                default -> handleText(userMessage, dbHistory, attachmentContext);
            };
        } catch (Exception e) {
            log.error("AI 聊天业务处理异常，intent={}", intent.intent, e);
            reply = AIChatReplyVO.builder()
                    .messageType("text")
                    .text("抱歉，处理你的请求时出了点问题，请稍后再试。")
                    .data(null)
                    .build();
        }

        // 6. 保存 AI 回复
        saveAssistantMessage(sessionId, reply);

        // 7. 更新会话最后活跃时间
        session.setLastActiveTime(Instant.now());
        sessionRepository.save(session);

        // 8. 组装响应
        List<String> suggestions = generateSuggestions(reply.getMessageType());

        return AIChatDataVO.builder()
                .sessionId(sessionId)
                .reply(reply)
                .suggestions(suggestions)
                .build();
    }

    // ======================== 意图识别 ========================

    /**
     * 调用 DeepSeek 进行意图识别。
     *
     * @param userMessage        用户当前输入
     * @param history            对话历史
     * @param attachmentContext  附件上下文文本（已格式化的引用信息）
     * @return 意图识别结果
     */
    private IntentResult recognizeIntent(String userMessage, List<AIChatHistoryMessage> history, String attachmentContext) {
        List<DeepSeekChatMessage> messages = new ArrayList<>();
        String intentPrompt = promptLoader.getPrompt("intent-recognition", INTENT_SYSTEM_PROMPT);
        messages.add(DeepSeekChatMessage.builder().role("system").content(intentPrompt).build());

        if (history != null && !history.isEmpty()) {
            for (AIChatHistoryMessage msg : history) {
                String normalizedRole = normalizeRole(msg.getRole());
                if (normalizedRole != null) {
                    messages.add(DeepSeekChatMessage.builder()
                            .role(normalizedRole)
                            .content(msg.getContent())
                            .build());
                }
            }
        }

        String fullMessage = userMessage + attachmentContext;
        messages.add(DeepSeekChatMessage.builder().role("user").content(fullMessage).build());

        log.info("AI 意图识别 Prompt：\n{}", JSONUtil.toJsonStr(messages));

        DeepSeekChatRequest request = DeepSeekChatRequest.builder()
                .messages(messages)
                .temperature(0.1)
                .responseFormat(Map.of("type", "json_object"))
                .build();

        String response = deepSeekService.chat(request);
        return parseIntentJson(response);
    }

    /**
     * 解析 DeepSeek 返回的意图 JSON。
     */
    private IntentResult parseIntentJson(String jsonStr) {
        String cleaned = AiResponseUtils.cleanJsonResponse(jsonStr);
        try {
            JSONObject root = JSONUtil.parseObj(cleaned);
            String intent = root.containsKey("intent") ? root.getStr("intent", "text") : "text";
            double confidence = root.containsKey("confidence") ? root.getDouble("confidence", 0.0) : 0.0;

            Map<String, Object> params = new HashMap<>();
            if (root.containsKey("params") && root.getJSONObject("params") != null) {
                JSONObject paramsNode = root.getJSONObject("params");
                for (String key : paramsNode.keySet()) {
                    Object value = paramsNode.get(key);
                    if (value == null || "null".equals(String.valueOf(value))) {
                        params.put(key, null);
                    } else if (value instanceof Number n) {
                        params.put(key, n.intValue());
                    } else {
                        params.put(key, String.valueOf(value));
                    }
                }
            }
            return new IntentResult(intent, params, confidence);
        } catch (Exception e) {
            log.warn("意图JSON解析失败，降级为text。原始响应：{}", cleaned, e);
            return new IntentResult("text", new HashMap<>(), 0.0);
        }
    }

    // ======================== 各类型 Handler ========================

    /**
     * 处理冰箱列表查询。
     */
    private AIChatReplyVO handleFridgeList() {
        List<FridgeVO> fridges = fridgeService.listMyFridges();

        List<Map<String, Object>> fridgeList = fridges.stream()
                .map(f -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", f.getId());
                    map.put("name", f.getFridgeName());
                    map.put("itemCount", f.getItemCount());
                    map.put("totalCapacity", f.getTotalCapacity());
                    map.put("status", f.getStatus());
                    map.put("isDefault", f.getIsDefault());
                    return map;
                })
                .toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", fridges.size());
        data.put("fridges", fridgeList);

        String text = fridges.isEmpty()
                ? "你目前还没有创建冰箱，快去添加一个吧！"
                : "你目前有 " + fridges.size() + " 个冰箱：";

        return AIChatReplyVO.builder()
                .messageType("fridge_list")
                .text(text)
                .data(data)
                .build();
    }

    /**
     * 处理物品列表查询。
     */
    private AIChatReplyVO handleItemList(Map<String, Object> params) {
        String keyword = (String) params.getOrDefault("keyword", null);
        String fridgeName = (String) params.getOrDefault("fridgeName", null);

        Long fridgeId = null;
        if (fridgeName != null && !fridgeName.isBlank()) {
            // 尝试根据名称查找冰箱
            List<FridgeVO> myFridges = fridgeService.listMyFridges();
            Optional<FridgeVO> matched = myFridges.stream()
                    .filter(f -> f.getFridgeName() != null && f.getFridgeName().contains(fridgeName))
                    .findFirst();
            if (matched.isPresent()) {
                fridgeId = matched.get().getId();
            }
        }

        ItemSearchRequest searchRequest = new ItemSearchRequest();
        searchRequest.setFridgeId(fridgeId);
        searchRequest.setKeyword(keyword);
        searchRequest.setSortField("storedDate");
        searchRequest.setSortOrder("desc");

        List<ItemVO> items = itemService.searchItems(searchRequest);

        // 批量查询冰箱名称（ItemVO 中不含 fridgeName）
        Set<Long> fridgeIds = items.stream().map(ItemVO::getFridgeId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> fridgeNameMap = fridgeRepository.findAllById(fridgeIds).stream()
                .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                .collect(Collectors.toMap(BizFridge::getId, BizFridge::getFridgeName));

        List<Map<String, Object>> itemList = items.stream()
                .map(item -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", item.getId());
                    map.put("name", item.getItemName());
                    map.put("num", item.getItemNum());
                    map.put("unit", item.getUnitName());
                    map.put("category", item.getCategoryName());
                    map.put("fridgeName", fridgeNameMap.getOrDefault(item.getFridgeId(), "未知"));
                    map.put("storedDate", item.getStoredDate() != null ? item.getStoredDate().toString() : null);

                    FreshnessStatus fs = calculateFreshnessStatus(item.getProductionDate(), item.getShelfLifeDays());
                    map.put("freshnessLabel", fs.label());
                    map.put("freshnessType", fs.type());
                    return map;
                })
                .toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", items.size());
        data.put("keyword", keyword);
        data.put("items", itemList);

        String text;
        if (items.isEmpty()) {
            text = keyword != null ? "没有找到与「" + keyword + "」相关的物品。" : "你的冰箱里暂时没有物品。";
        } else {
            text = (keyword != null ? "找到 " + items.size() + " 个与「" + keyword + "」相关的物品：" : "找到 " + items.size() + " 个物品：");
        }

        return AIChatReplyVO.builder()
                .messageType("item_list")
                .text(text)
                .data(data)
                .build();
    }

    /**
     * 处理临期/过期提醒。
     */
    private AIChatReplyVO handleExpiringAlert() {
        Long currentUserId = getCurrentUserId();

        // 获取当前用户所有冰箱ID
        List<Long> fridgeIds = fridgeRepository.findByOwnerIdAndIsDeletedFalse(currentUserId, Sort.unsorted())
                .stream().map(BizFridge::getId).toList();

        if (fridgeIds.isEmpty()) {
            return AIChatReplyVO.builder()
                    .messageType("expiring_alert")
                    .text("你目前还没有冰箱，快去添加一个吧！")
                    .data(Map.of("expiringCount", 0, "expiredCount", 0, "total", 0, "items", List.of()))
                    .build();
        }

        List<BizFridgeItem> candidates = itemRepository.findExpiringCandidates(fridgeIds);

        // 批量查询关联信息
        Set<Long> unitIds = candidates.stream().map(BizFridgeItem::getItemUnitId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> unitNameMap = unitRepository.findAllById(unitIds).stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .collect(Collectors.toMap(BizItemUnit::getId, BizItemUnit::getUnitName));

        Map<Long, String> fridgeNameMap = fridgeRepository.findAllById(fridgeIds).stream()
                .collect(Collectors.toMap(BizFridge::getId, BizFridge::getFridgeName));

        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
        List<Map<String, Object>> alertItems = new ArrayList<>();
        int expiredCount = 0;
        int expiringCount = 0;

        for (BizFridgeItem item : candidates) {
            long diffDays = ChronoUnit.DAYS.between(item.getProductionDate(), today);
            int remainingDays = item.getShelfLifeDays() - (int) diffDays;
            double r = ((double) remainingDays / item.getShelfLifeDays()) * 100.0;

            String freshnessLabel;
            String freshnessType;
            if (r <= 0) {
                freshnessLabel = "已过期";
                freshnessType = "danger";
                expiredCount++;
            } else if (r < 20) {
                freshnessLabel = "临期";
                freshnessType = "warning";
                expiringCount++;
            } else {
                continue; // 不纳入统计
            }

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("name", item.getItemName());
            map.put("num", item.getItemNum());
            map.put("unit", unitNameMap.getOrDefault(item.getItemUnitId(), ""));
            map.put("fridgeName", fridgeNameMap.getOrDefault(item.getFridgeId(), "未知"));
            map.put("remainingDays", remainingDays);
            map.put("freshnessLabel", freshnessLabel);
            map.put("freshnessType", freshnessType);
            alertItems.add(map);
        }

        int total = expiredCount + expiringCount;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("expiringCount", expiringCount);
        data.put("expiredCount", expiredCount);
        data.put("total", total);
        data.put("items", alertItems);

        String text;
        if (total == 0) {
            text = "🎉 没有发现临期或过期的物品，继续保持！";
        } else {
            text = "⚠️ 发现 " + total + " 件需要关注的物品（" + expiringCount + " 件临期，" + expiredCount + " 件已过期）：";
        }

        return AIChatReplyVO.builder()
                .messageType("expiring_alert")
                .text(text)
                .data(data)
                .build();
    }

    /**
     * 处理菜谱推荐。
     * <p>优先基于用户指定的附件食材推荐，全量库存作为补充参考。将用户原话传入，使 LLM 能理解数量要求。</p>
     *
     * @param userMessage 用户原始输入（含数量要求等）
     * @param attachments 用户引用的附件列表
     */
    private AIChatReplyVO handleRecipeRecommend(String userMessage, List<AIChatAttachment> attachments) {
        Long currentUserId = getCurrentUserId();

        // 1. 提取用户指定的食材（附件中的 item）
        List<String> specifiedItems = new ArrayList<>();
        if (attachments != null && !attachments.isEmpty()) {
            for (AIChatAttachment att : attachments) {
                if ("item".equals(att.getType()) && att.getName() != null && !att.getName().isBlank()) {
                    specifiedItems.add(att.getName());
                }
            }
        }

        // 2. 获取全量库存作为补充参考
        List<Long> fridgeIds = fridgeRepository.findByOwnerIdAndIsDeletedFalse(currentUserId, Sort.unsorted())
                .stream().map(BizFridge::getId).toList();

        Set<String> inventoryItems = new HashSet<>();
        if (!fridgeIds.isEmpty()) {
            List<BizFridgeItem> items = itemRepository.searchItems(fridgeIds, "", null, null, null, Sort.unsorted());
            inventoryItems = items.stream()
                    .map(BizFridgeItem::getItemName)
                    .filter(name -> name != null && !name.isBlank())
                    .collect(Collectors.toSet());
        }

        // 3. 构建 Prompt
        StringBuilder promptBuilder = new StringBuilder();
        if (!specifiedItems.isEmpty()) {
            promptBuilder.append("用户特别指定了以下食材，请优先基于这些食材推荐菜谱：")
                    .append(String.join("、", specifiedItems)).append("。\n");
        }
        if (!inventoryItems.isEmpty()) {
            promptBuilder.append("用户冰箱中的其他食材还有：")
                    .append(String.join("、", inventoryItems)).append("。\n");
        }
        if (specifiedItems.isEmpty() && inventoryItems.isEmpty()) {
            promptBuilder.append("用户冰箱目前没有食材。\n");
        }
        promptBuilder.append("用户原话：").append(userMessage).append("\n");
        promptBuilder.append("请根据以上信息推荐适合的家常菜。");

        String userPrompt = promptBuilder.toString();
        log.info("AI 菜谱推荐 Prompt：\n{}", userPrompt);

        List<DeepSeekChatMessage> recipeMessages = new ArrayList<>();
        String recipePrompt = promptLoader.getPrompt("recipe-recommendation", RECIPE_SYSTEM_PROMPT);
        recipeMessages.add(DeepSeekChatMessage.builder().role("system").content(recipePrompt).build());
        recipeMessages.add(DeepSeekChatMessage.builder().role("user").content(userPrompt).build());

        DeepSeekChatRequest request = DeepSeekChatRequest.builder()
                .messages(recipeMessages)
                .temperature(0.6)
                .responseFormat(Map.of("type", "json_object"))
                .build();

        String response = deepSeekService.chat(request);
        String cleaned = AiResponseUtils.cleanJsonResponse(response);

        try {
            JSONObject root = JSONUtil.parseObj(cleaned);
            String text = root.containsKey("text") ? root.getStr("text", "为你推荐以下菜谱：") : "为你推荐以下菜谱：";

            List<Map<String, Object>> recipes = new ArrayList<>();
            if (root.containsKey("recipes") && root.getJSONArray("recipes") != null) {
                JSONArray recipesArray = root.getJSONArray("recipes");
                for (int i = 0; i < recipesArray.size(); i++) {
                    JSONObject recipeNode = recipesArray.getJSONObject(i);
                    Map<String, Object> recipe = new LinkedHashMap<>();
                    recipe.put("name", getJsonText(recipeNode, "name", "未知菜谱"));
                    recipe.put("difficulty", getJsonText(recipeNode, "difficulty", "简单"));
                    recipe.put("cookTime", getJsonText(recipeNode, "cookTime", "未知"));
                    recipe.put("matchedItems", getJsonStringList(recipeNode, "matchedItems"));
                    recipe.put("missingItems", getJsonStringList(recipeNode, "missingItems"));
                    recipe.put("description", getJsonText(recipeNode, "description", ""));
                    recipes.add(recipe);
                }
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("recipes", recipes);

            return AIChatReplyVO.builder()
                    .messageType("recipe_recommend")
                    .text(text)
                    .data(data)
                    .build();
        } catch (Exception e) {
            log.warn("菜谱推荐JSON解析失败，降级为纯文本。原始响应：{}", cleaned, e);
            return AIChatReplyVO.builder()
                    .messageType("text")
                    .text("抱歉，菜谱推荐生成失败，请稍后再试。")
                    .data(null)
                    .build();
        }
    }

    /**
     * 处理趋势图表查询。
     */
    private AIChatReplyVO handleTrendChart(Map<String, Object> params) {
        String type = (String) params.getOrDefault("type", "both");
        int days = params.get("days") instanceof Number n ? n.intValue() : 7;
        if (days != 7 && days != 30) {
            days = 7;
        }

        Long currentUserId = getCurrentUserId();
        List<Long> fridgeIds = fridgeRepository.findByOwnerIdAndIsDeletedFalse(currentUserId, Sort.unsorted())
                .stream().map(BizFridge::getId).toList();

        // 默认取所有冰箱的数据（fridgeId 传 null）
        Long fridgeId = null;

        List<TakeOutDailyStatisticsVO> takeOutStats = List.of();
        List<TakeOutDailyStatisticsVO> addStats = List.of();

        if ("take_out".equals(type) || "both".equals(type)) {
            takeOutStats = itemService.getRecent30DaysTakeOutStatistics(fridgeId);
        }
        if ("add".equals(type) || "both".equals(type)) {
            addStats = itemService.getRecent30DaysAddStatistics(fridgeId);
        }

        // 截取最近 N 天
        if (days == 7) {
            if (takeOutStats.size() > 7) {
                takeOutStats = takeOutStats.subList(takeOutStats.size() - 7, takeOutStats.size());
            }
            if (addStats.size() > 7) {
                addStats = addStats.subList(addStats.size() - 7, addStats.size());
            }
        }

        // 构建日期列表（以 takeOutStats 或 addStats 为基准）
        List<String> dates = new ArrayList<>();
        List<Map<String, Object>> series = new ArrayList<>();

        if (!takeOutStats.isEmpty()) {
            dates = takeOutStats.stream()
                    .map(s -> {
                        LocalDate d = LocalDate.parse(s.getDate());
                        return d.format(CHART_DATE_FORMATTER);
                    })
                    .toList();
        } else if (!addStats.isEmpty()) {
            dates = addStats.stream()
                    .map(s -> {
                        LocalDate d = LocalDate.parse(s.getDate());
                        return d.format(CHART_DATE_FORMATTER);
                    })
                    .toList();
        }

        if ("take_out".equals(type) || "both".equals(type)) {
            Map<String, Object> seriesMap = new LinkedHashMap<>();
            seriesMap.put("name", "取出");
            seriesMap.put("color", "#64B5F6");
            seriesMap.put("counts", takeOutStats.stream().map(TakeOutDailyStatisticsVO::getCount).toList());
            series.add(seriesMap);
        }
        if ("add".equals(type) || "both".equals(type)) {
            Map<String, Object> seriesMap = new LinkedHashMap<>();
            seriesMap.put("name", "入库");
            seriesMap.put("color", "#81C784");
            seriesMap.put("counts", addStats.stream().map(TakeOutDailyStatisticsVO::getCount).toList());
            series.add(seriesMap);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("chartType", "line");
        data.put("title", "近" + days + "天趋势");
        data.put("dates", dates);
        data.put("series", series);

        String text = "这是你近 " + days + " 天的" + ("both".equals(type) ? "取出/入库" : ("take_out".equals(type) ? "取出" : "入库")) + "趋势：";

        return AIChatReplyVO.builder()
                .messageType("trend_chart")
                .text(text)
                .data(data)
                .build();
    }

    /**
     * 处理操作确认。
     */
    private AIChatReplyVO handleActionConfirm(Map<String, Object> params) {
        String action = (String) params.getOrDefault("action", "");
        String targetName = (String) params.getOrDefault("targetName", "");

        Long currentUserId = getCurrentUserId();

        // 尝试查找目标对象
        Long targetId = null;
        int affectedCount = 0;
        String riskLevel = "medium";

        if ("delete_fridge".equals(action) && targetName != null && !targetName.isBlank()) {
            List<FridgeVO> fridges = fridgeService.listMyFridges();
            final String searchName = targetName;
            Optional<FridgeVO> matched = fridges.stream()
                    .filter(f -> f.getFridgeName() != null && f.getFridgeName().contains(searchName))
                    .findFirst();
            if (matched.isPresent()) {
                targetId = matched.get().getId();
                affectedCount = matched.get().getItemCount();
                riskLevel = affectedCount > 0 ? "high" : "medium";
            }
        } else if ("clear_expired".equals(action)) {
            targetName = "所有过期物品";
            riskLevel = "medium";
            // 统计过期数量
            // 简化处理，不精确统计
        }

        if (targetId == null && !"clear_expired".equals(action)) {
            // 找不到目标，降级为文本
            return AIChatReplyVO.builder()
                    .messageType("text")
                    .text("抱歉，没有找到你要操作的目标「" + targetName + "」，请确认名称是否正确。")
                    .data(null)
                    .build();
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", action);
        data.put("targetId", targetId);
        data.put("targetName", targetName);
        data.put("riskLevel", riskLevel);
        data.put("affectedCount", affectedCount);

        String text;
        if ("delete_fridge".equals(action)) {
            text = "确定要删除「" + targetName + "」吗？该操作不可撤销" +
                    (affectedCount > 0 ? "，冰箱内的 " + affectedCount + " 件物品也将被清空。" : "。");
        } else {
            text = "确定要" + ("clear_expired".equals(action) ? "清空所有过期物品" : "执行此操作") + "吗？";
        }

        return AIChatReplyVO.builder()
                .messageType("action_confirm")
                .text(text)
                .data(data)
                .build();
    }

    /**
     * 处理通用文本对话。
     */
    private AIChatReplyVO handleText(String userMessage, List<AIChatHistoryMessage> history, String attachmentContext) {
        List<DeepSeekChatMessage> messages = new ArrayList<>();

        String chatPrompt = promptLoader.getPrompt("general-chat", GENERAL_CHAT_SYSTEM_PROMPT);
        messages.add(DeepSeekChatMessage.builder().role("system").content(chatPrompt).build());

        if (history != null && !history.isEmpty()) {
            for (AIChatHistoryMessage msg : history) {
                String normalizedRole = normalizeRole(msg.getRole());
                if (normalizedRole != null) {
                    messages.add(DeepSeekChatMessage.builder()
                            .role(normalizedRole)
                            .content(msg.getContent())
                            .build());
                }
            }
        }

        String fullMessage = userMessage + attachmentContext;
        messages.add(DeepSeekChatMessage.builder().role("user").content(fullMessage).build());

        log.info("AI 通用对话 Prompt：\n{}", JSONUtil.toJsonStr(messages));

        DeepSeekChatRequest request = DeepSeekChatRequest.builder()
                .messages(messages)
                .temperature(0.7)
                .build();

        String response = deepSeekService.chat(request);

        return AIChatReplyVO.builder()
                .messageType("text")
                .text(response)
                .data(null)
                .build();
    }

    // ======================== 会话管理方法 ========================

    /**
     * {@inheritDoc}
     * <p>查询当前用户未删除的会话列表，按最后活跃时间降序排列。</p>
     */
    @Override
    public List<AIChatSessionVO> listSessions() {
        Long currentUserId = getCurrentUserId();
        List<AiChatSession> sessions = sessionRepository
                .findByUserIdAndIsDeletedOrderByLastActiveTimeDesc(currentUserId, (byte) 0);
        return sessions.stream()
                .map(s -> AIChatSessionVO.builder()
                        .sessionId(s.getSessionId())
                        .title(s.getTitle())
                        .lastActiveTime(formatInstant(s.getLastActiveTime()))
                        .build())
                .toList();
    }

    /**
     * {@inheritDoc}
     * <p>将会话的 is_deleted 标记为 1（软删除）。</p>
     */
    @Override
    public void deleteSession(String sessionId) {
        Long currentUserId = getCurrentUserId();
        AiChatSession session = sessionRepository
                .findBySessionIdAndUserIdAndIsDeleted(sessionId, currentUserId, (byte) 0)
                .orElseThrow(BusinessException::notFound);
        session.setIsDeleted((byte) 1);
        sessionRepository.save(session);
        log.info("用户 {} 软删除会话 {}", currentUserId, sessionId);
    }

    /**
     * {@inheritDoc}
     * <p>查询指定会话的历史消息，按时间升序排列。</p>
     */
    @Override
    public List<AIChatMessageVO> getSessionMessages(String sessionId) {
        Long currentUserId = getCurrentUserId();

        // 校验会话归属权（只能查看自己的会话）
        sessionRepository.findBySessionIdAndUserIdAndIsDeleted(sessionId, currentUserId, (byte) 0)
                .orElseThrow(BusinessException::notFound);

        List<AiChatMessage> messages = messageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);

        return messages.stream()
                .map(m -> AIChatMessageVO.builder()
                        .role(m.getRole())
                        .content(m.getContent())
                        .messageType(m.getMessageType())
                        .data(m.getStructuredData())
                        .attachments(m.getAttachments())
                        .createTime(formatInstant(m.getCreateTime()))
                        .build())
                .toList();
    }

    /**
     * 创建新的 AI 聊天会话。
     */
    private AiChatSession createNewSession(String sessionId, Long userId, String firstMessage) {
        String title = firstMessage != null && firstMessage.length() > 20
                ? firstMessage.substring(0, 20) + "..."
                : firstMessage;
        Instant now = Instant.now();
        AiChatSession session = AiChatSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .title(title)
                .lastActiveTime(now)
                .isDeleted((byte) 0)
                .createTime(now)
                .updateTime(now)
                .build();
        return sessionRepository.save(session);
    }

    /**
     * 保存用户消息到数据库（支持附件）。
     */
    private void saveUserMessage(String sessionId, String content, List<AIChatAttachment> attachments) {
        List<Map<String, Object>> attachmentMaps = null;
        if (attachments != null && !attachments.isEmpty()) {
            attachmentMaps = attachments.stream().map(att -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("type", att.getType());
                map.put("id", att.getId());
                map.put("name", att.getName());
                if (att.getFridgeId() != null) {
                    map.put("fridgeId", att.getFridgeId());
                }
                if (att.getFridgeName() != null) {
                    map.put("fridgeName", att.getFridgeName());
                }
                return map;
            }).toList();
        }

        AiChatMessage msg = AiChatMessage.builder()
                .sessionId(sessionId)
                .role("user")
                .content(content)
                .attachments(attachmentMaps)
                .createTime(Instant.now())
                .build();
        messageRepository.save(msg);
    }

    /**
     * 保存 AI 回复消息到数据库。
     */
    private void saveAssistantMessage(String sessionId, AIChatReplyVO reply) {
        Map<String, Object> structuredData = null;
        if (reply.getData() instanceof Map<?, ?> map) {
            structuredData = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                structuredData.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }

        AiChatMessage msg = AiChatMessage.builder()
                .sessionId(sessionId)
                .role("assistant")
                .content(reply.getText())
                .messageType(reply.getMessageType())
                .structuredData(structuredData)
                .createTime(Instant.now())
                .build();
        messageRepository.save(msg);
    }

    /**
     * 从数据库加载指定会话的历史消息，转换为前端 history 格式。
     * <p>用户历史消息若包含附件，会将附件上下文还原到 content 中，确保 LLM 理解完整上下文。</p>
     */
    private List<AIChatHistoryMessage> buildHistoryFromDb(String sessionId) {
        List<AiChatMessage> messages = messageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);

        // 滑动窗口截断：保留最近 N 轮对话（1轮 = 1问1答，约2条消息）
        int effectiveRounds = (maxHistoryRounds != null && maxHistoryRounds > 0) ? maxHistoryRounds : 10;
        int maxMessages = effectiveRounds * 2;

        List<AiChatMessage> recentMessages = messages;
        if (messages.size() > maxMessages) {
            recentMessages = messages.subList(messages.size() - maxMessages, messages.size());
            log.info("AI 历史消息截断：sessionId={}，原{}条，保留最近{}条（{}轮）",
                    sessionId, messages.size(), maxMessages, effectiveRounds);
        }

        return recentMessages.stream()
                .map(m -> {
                    String content = m.getContent() != null ? m.getContent() : "";
                    if ("user".equals(m.getRole()) && m.getAttachments() != null && !m.getAttachments().isEmpty()) {
                        content = content + formatAttachmentContextFromDb(m.getAttachments());
                    }
                    return AIChatHistoryMessage.builder()
                            .role(m.getRole())
                            .content(content)
                            .build();
                })
                .toList();
    }

    /**
     * 格式化 Instant 为上海时区的日期时间字符串。
     */
    private String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZONE_ID_SHANGHAI)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ======================== 辅助方法 ========================

    /**
     * 计算物品新鲜度状态。
     */
    private FreshnessStatus calculateFreshnessStatus(LocalDate productionDate, Integer shelfLifeDays) {
        if (productionDate == null || shelfLifeDays == null) {
            return new FreshnessStatus("未知", "info", null);
        }
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
        long diffDays = ChronoUnit.DAYS.between(productionDate, today);
        int remainingDays = shelfLifeDays - (int) diffDays;
        if (shelfLifeDays > 30) {
            return new FreshnessStatus("长保质期", "info", remainingDays);
        }
        double r = ((double) remainingDays / shelfLifeDays) * 100.0;

        if (r <= 0) {
            return new FreshnessStatus("已过期", "danger", remainingDays);
        } else if (r < 20) {
            return new FreshnessStatus("临期", "warning", remainingDays);
        } else if (r < 50) {
            return new FreshnessStatus("一般", "primary", remainingDays);
        } else {
            return new FreshnessStatus("新鲜", "success", remainingDays);
        }
    }

    /**
     * 根据消息类型生成建议按钮。
     */
    private List<String> generateSuggestions(String messageType) {
        return switch (messageType) {
            case "fridge_list" -> List.of("查看物品", "临期提醒", "推荐菜谱");
            case "item_list" -> List.of("查看冰箱", "临期提醒", "推荐菜谱");
            case "expiring_alert" -> List.of("查看冰箱", "推荐菜谱处理临期食材");
            case "recipe_recommend" -> List.of("查看冰箱", "还有什么菜谱");
            case "trend_chart" -> List.of("查看冰箱", "临期提醒");
            case "action_confirm" -> List.of();
            case "fridge_creation_wizard" -> List.of();
            default -> List.of("查看冰箱", "临期提醒", "推荐菜谱");
        };
    }

    // ======================== 附件相关方法 ========================

    /**
     * 校验用户引用的附件是否属于当前登录用户。
     *
     * @param attachments 附件列表
     * @param userId      当前用户ID
     */
    private void validateAttachments(List<AIChatAttachment> attachments, Long userId) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        List<BizFridge> myFridges = fridgeRepository.findByOwnerIdAndIsDeletedFalse(userId, Sort.unsorted());
        Set<Long> myFridgeIdSet = myFridges.stream().map(BizFridge::getId).collect(Collectors.toSet());

        for (AIChatAttachment att : attachments) {
            if ("fridge".equals(att.getType())) {
                if (!myFridgeIdSet.contains(att.getId())) {
                    throw BusinessException.forbidden();
                }
            } else if ("item".equals(att.getType())) {
                BizFridgeItem item = itemRepository.findById(att.getId()).orElse(null);
                if (item == null || Boolean.TRUE.equals(item.getIsDeleted()) || !myFridgeIdSet.contains(item.getFridgeId())) {
                    throw BusinessException.forbidden();
                }
            }
        }
    }

    /**
     * 将附件列表格式化为 LLM 可读的上下文文本。
     * <p>基于附件 ID 查询数据库补充实时业务数据（物品保质期、冰箱库存等），使 AI 回答更精准。</p>
     *
     * @param attachments 附件列表
     * @return 格式化后的上下文文本，无附件时返回空字符串
     */
    private String buildAttachmentContext(List<AIChatAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder("\n\n【用户引用的上下文】\n");
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);

        for (AIChatAttachment att : attachments) {
            if ("fridge".equals(att.getType())) {
                BizFridge fridge = fridgeRepository.findById(att.getId()).orElse(null);
                if (fridge != null && !Boolean.TRUE.equals(fridge.getIsDeleted())) {
                    List<BizFridgeItem> items = itemRepository.findByFridgeIdAndIsDeletedFalse(fridge.getId());
                    List<String> expiredDetails = new ArrayList<>();
                    List<String> expiringDetails = new ArrayList<>();
                    List<String> normalNames = new ArrayList<>();
                    List<String> freshNames = new ArrayList<>();
                    List<String> unknownNames = new ArrayList<>();

                    for (BizFridgeItem item : items) {
                        if (item.getProductionDate() == null || item.getShelfLifeDays() == null) {
                            unknownNames.add(item.getItemName());
                            continue;
                        }
                        long diffDays = ChronoUnit.DAYS.between(item.getProductionDate(), today);
                        int remainingDays = item.getShelfLifeDays() - (int) diffDays;
                        FreshnessStatus fs = calculateFreshnessStatus(item.getProductionDate(), item.getShelfLifeDays());
                        String detail = item.getItemName() + "（剩余：" + remainingDays + " 天，" + fs.label() + "）";
                        switch (fs.label()) {
                            case "已过期" -> expiredDetails.add(detail);
                            case "临期" -> expiringDetails.add(detail);
                            case "一般" -> normalNames.add(item.getItemName());
                            case "新鲜", "长保质期" -> freshNames.add(item.getItemName());
                            default -> unknownNames.add(item.getItemName());
                        }
                    }

                    sb.append("- 冰箱：").append(fridge.getFridgeName()).append(" (ID: ").append(fridge.getId()).append(")\n");
                    sb.append("  物品总数：").append(items.size()).append(" 件");
                    if (!expiredDetails.isEmpty()) sb.append("，已过期：").append(expiredDetails.size()).append(" 件");
                    if (!expiringDetails.isEmpty()) sb.append("，临期：").append(expiringDetails.size()).append(" 件");
                    if (!normalNames.isEmpty()) sb.append("，一般：").append(normalNames.size()).append(" 件");
                    if (!freshNames.isEmpty()) sb.append("，新鲜：").append(freshNames.size()).append(" 件");
                    if (!unknownNames.isEmpty()) sb.append("，未知：").append(unknownNames.size()).append(" 件");
                    sb.append("\n");

                    if (!expiredDetails.isEmpty() || !expiringDetails.isEmpty()) {
                        sb.append("  临期/过期物品：\n");
                        for (String detail : expiredDetails) {
                            sb.append("  - ").append(detail).append("\n");
                        }
                        for (String detail : expiringDetails) {
                            sb.append("  - ").append(detail).append("\n");
                        }
                    }

                    List<String> otherItems = new ArrayList<>();
                    otherItems.addAll(normalNames);
                    otherItems.addAll(freshNames);
                    otherItems.addAll(unknownNames);
                    if (!otherItems.isEmpty()) {
                        sb.append("  其他物品：").append(String.join("、", otherItems)).append("\n");
                    }
                } else {
                    sb.append("- 冰箱：").append(att.getName()).append(" (ID: ").append(att.getId()).append(")\n");
                }
            } else if ("item".equals(att.getType())) {
                BizFridgeItem item = itemRepository.findById(att.getId()).orElse(null);
                if (item != null && !Boolean.TRUE.equals(item.getIsDeleted())) {
                    String unitName = "";
                    if (item.getItemUnitId() != null) {
                        unitName = unitRepository.findById(item.getItemUnitId())
                                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                                .map(BizItemUnit::getUnitName).orElse("");
                    }
                    String fridgeName = "";
                    if (item.getFridgeId() != null) {
                        fridgeName = fridgeRepository.findById(item.getFridgeId())
                                .filter(f -> !Boolean.TRUE.equals(f.getIsDeleted()))
                                .map(BizFridge::getFridgeName).orElse("未知");
                    }

                    sb.append("- 物品：").append(item.getItemName())
                            .append("，数量：").append(item.getItemNum()).append(" ").append(unitName);

                    if (item.getProductionDate() != null && item.getShelfLifeDays() != null) {
                        long diffDays = ChronoUnit.DAYS.between(item.getProductionDate(), today);
                        int remainingDays = item.getShelfLifeDays() - (int) diffDays;
                        FreshnessStatus fs = calculateFreshnessStatus(item.getProductionDate(), item.getShelfLifeDays());
                        sb.append("，生产日期：").append(item.getProductionDate())
                                .append("，保质期：").append(item.getShelfLifeDays()).append(" 天")
                                .append("，剩余：").append(remainingDays).append(" 天")
                                .append("（").append(fs.label()).append("）");
                    }
                    sb.append("，所属冰箱：").append(fridgeName).append("\n");
                } else {
                    sb.append("- 物品：").append(att.getName()).append(" (ID: ").append(att.getId()).append(")\n");
                }
            }
        }
        return sb.toString();
    }

    /**
     * 从数据库附件快照还原为 LLM 上下文文本（用于历史消息）。
     *
     * @param attachments 数据库中的附件快照列表
     * @return 格式化后的上下文文本
     */
    private String formatAttachmentContextFromDb(List<Map<String, Object>> attachments) {
        StringBuilder sb = new StringBuilder("\n\n【用户引用的上下文】\n");
        for (Map<String, Object> att : attachments) {
            String type = (String) att.get("type");
            Object id = att.get("id");
            String name = (String) att.get("name");
            if ("fridge".equals(type)) {
                sb.append("- 冰箱：").append(name).append(" (ID: ").append(id).append(")\n");
            } else if ("item".equals(type)) {
                String fridgeName = (String) att.get("fridgeName");
                sb.append("- 物品：").append(name).append(" (ID: ").append(id).append(")");
                if (fridgeName != null && !fridgeName.isBlank()) {
                    sb.append("，所属冰箱：").append(fridgeName);
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 处理空消息场景：若用户未输入文字但携带附件，自动补充默认提示。
     *
     * @param message     原始消息
     * @param attachments 附件列表
     * @return 处理后的消息内容
     */
    private String resolveUserMessage(String message, List<AIChatAttachment> attachments) {
        if (message != null && !message.isBlank()) {
            return message;
        }
        if (attachments != null && !attachments.isEmpty()) {
            return "请帮我分析一下这些信息。";
        }
        return message != null ? message : "";
    }

    /**
     * 规范化消息角色，将前端可能的非法 role 映射为 DeepSeek 支持的值。
     * <p>DeepSeek 支持的 role：system, user, assistant, tool。</p>
     *
     * @param role 原始 role
     * @return 规范化后的 role，如果不合法则返回 null（表示应丢弃该消息）
     */
    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        return switch (role.toLowerCase()) {
            case "system" -> "system";
            case "user" -> "user";
            case "assistant", "ai" -> "assistant";
            case "tool" -> "tool";
            default -> null;
        };
    }

    /**
     * 获取当前登录用户ID。
     */
    private Long getCurrentUserId() {
        String username = getCurrentUsername();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);
        return user.getId();
    }

    /**
     * 从 SecurityContext 获取当前登录用户名。
     */
    private static String getCurrentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw BusinessException.authFailed();
        }
        return authentication.getName();
    }

    /**
     * 安全获取 JSON 文本字段。
     */
    private String getJsonText(JSONObject node, String field, String defaultValue) {
        return node.containsKey(field) && node.get(field) != null ? node.getStr(field, defaultValue) : defaultValue;
    }

    /**
     * 安全获取 JSON 字符串列表。
     */
    private List<String> getJsonStringList(JSONObject node, String field) {
        List<String> list = new ArrayList<>();
        if (node.containsKey(field) && node.getJSONArray(field) != null) {
            JSONArray array = node.getJSONArray(field);
            for (Object item : array) {
                if (item != null) {
                    list.add(String.valueOf(item));
                }
            }
        }
        return list;
    }

    /**
         * 意图识别结果内部类。
         */
        private record IntentResult(String intent, Map<String, Object> params, double confidence) {
            private IntentResult(String intent, Map<String, Object> params, double confidence) {
                this.intent = intent != null ? intent : "text";
                this.params = params != null ? params : new HashMap<>();
                this.confidence = confidence;
            }
        }

    // ======================== 流式输出方法 ========================

    @Override
    public void streamChat(AIChatRequest request, SseEmitter emitter) {
        Long currentUserId;
        SecurityContext securityContext;
        try {
            currentUserId = getCurrentUserId();
            securityContext = SecurityContextHolder.getContext();
        } catch (Exception e) {
            sendStreamError(emitter, e);
            return;
        }

        CompletableFuture.runAsync(() -> {
            SecurityContextHolder.setContext(securityContext);
            try {
                streamChatInternal(request, emitter, currentUserId);
            } catch (Exception e) {
                log.error("AI 流式聊天异常", e);
                sendStreamError(emitter, e);
            } finally {
                SecurityContextHolder.clearContext();
            }
        });
    }

    private void streamChatInternal(AIChatRequest request, SseEmitter emitter, Long currentUserId) throws Exception {
        String sessionId = request.getSessionId();
        boolean isNewSession = false;

        // 1. 查找或创建会话
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            isNewSession = true;
        }

        AiChatSession session;
        if (!isNewSession) {
            Optional<AiChatSession> existing = sessionRepository
                    .findBySessionIdAndUserIdAndIsDeleted(sessionId, currentUserId, (byte) 0);
            if (existing.isPresent()) {
                session = existing.get();
            } else {
                session = createNewSession(sessionId, currentUserId, request.getMessage());
            }
        } else {
            session = createNewSession(sessionId, currentUserId, request.getMessage());
        }

        // 2. 加载历史
        List<AIChatHistoryMessage> dbHistory = buildHistoryFromDb(sessionId);

        // 3. 校验附件权限
        validateAttachments(request.getAttachments(), currentUserId);

        // 4. 处理空消息并构建附件上下文
        String userMessage = resolveUserMessage(request.getMessage(), request.getAttachments());
        String attachmentContext = buildAttachmentContext(request.getAttachments());

        // 5. 保存用户消息
        saveUserMessage(sessionId, userMessage, request.getAttachments());

        // 预声明 fullText（向导流程中也会用到）
        StringBuilder fullText = new StringBuilder();

        // === 向导流程优先处理：若前端处于冰箱创建向导中，直接后端状态驱动推进 ===
        if (request.getWizardContext() != null
                && "fridge_creation".equals(request.getWizardContext().getType())) {
            AIChatReplyVO reply = handleFridgeCreationWizard(request);
            streamStructuredReply(emitter, reply, fullText);
            saveAssistantMessage(sessionId, reply);
            session.setLastActiveTime(Instant.now());
            sessionRepository.save(session);
            List<String> suggestions = generateSuggestions(reply.getMessageType());
            Map<String, Object> doneEvent = new LinkedHashMap<>();
            doneEvent.put("sessionId", sessionId);
            doneEvent.put("suggestions", suggestions);
            emitter.send(SseEmitter.event().name("done").data(doneEvent));
            emitter.complete();
            return;
        }

        // 6. 同步意图识别
        IntentResult intent = recognizeIntent(userMessage, dbHistory, attachmentContext);
        log.info("AI 流式聊天意图识别结果：intent={}, params={}, confidence={}",
                intent.intent, intent.params, intent.confidence);

        // 5. 根据意图分发处理
        AIChatReplyVO reply;

        try {
            reply = switch (intent.intent) {
                case "fridge_list" -> {
                    AIChatReplyVO r = handleFridgeList();
                    streamStructuredReply(emitter, r, fullText);
                    yield r;
                }
                case "item_list" -> {
                    AIChatReplyVO r = handleItemList(intent.params);
                    streamStructuredReply(emitter, r, fullText);
                    yield r;
                }
                case "expiring_alert" -> {
                    AIChatReplyVO r = handleExpiringAlert();
                    streamStructuredReply(emitter, r, fullText);
                    yield r;
                }
                case "recipe_recommend" -> {
                    AIChatReplyVO r = handleRecipeRecommend(userMessage, request.getAttachments());
                    streamStructuredReply(emitter, r, fullText);
                    yield r;
                }
                case "trend_chart" -> {
                    AIChatReplyVO r = handleTrendChart(intent.params);
                    streamStructuredReply(emitter, r, fullText);
                    yield r;
                }
                case "action_confirm" -> {
                    AIChatReplyVO r = handleActionConfirm(intent.params);
                    streamStructuredReply(emitter, r, fullText);
                    yield r;
                }
                case "fridge_creation_wizard" -> {
                    AIChatReplyVO r = handleFridgeCreationWizardInit();
                    streamStructuredReply(emitter, r, fullText);
                    yield r;
                }
                default -> streamTextReply(userMessage, dbHistory, emitter, fullText, attachmentContext);
            };
        } catch (Exception e) {
            log.error("AI 流式聊天业务处理异常，intent={}", intent.intent, e);
            reply = AIChatReplyVO.builder()
                    .messageType("text")
                    .text("抱歉，处理你的请求时出了点问题，请稍后再试。")
                    .data(null)
                    .build();
            streamStructuredReply(emitter, reply, fullText);
        }

        // 6. 保存 AI 回复
        saveAssistantMessage(sessionId, reply);

        // 7. 更新会话
        session.setLastActiveTime(Instant.now());
        sessionRepository.save(session);

        // 8. 发送 done 事件
        List<String> suggestions = generateSuggestions(reply.getMessageType());
        Map<String, Object> doneEvent = new LinkedHashMap<>();
        doneEvent.put("sessionId", sessionId);
        doneEvent.put("suggestions", suggestions);
        emitter.send(SseEmitter.event().name("done").data(doneEvent));
        emitter.complete();
    }

    private void streamStructuredReply(SseEmitter emitter, AIChatReplyVO reply, StringBuilder fullText) throws IOException {
        String text = reply.getText();
        if (text != null && !text.isEmpty()) {
            Map<String, Object> textEvent = new LinkedHashMap<>();
            textEvent.put("chunk", text);
            emitter.send(SseEmitter.event().name("text").data(textEvent));
            fullText.append(text);
        }

        Map<String, Object> cardEvent = new LinkedHashMap<>();
        cardEvent.put("messageType", reply.getMessageType());
        cardEvent.put("data", reply.getData());
        emitter.send(SseEmitter.event().name("card").data(cardEvent));
    }

    private AIChatReplyVO streamTextReply(String userMessage, List<AIChatHistoryMessage> history,
                                          SseEmitter emitter, StringBuilder fullText,
                                          String attachmentContext) throws IOException {
        List<DeepSeekChatMessage> messages = new ArrayList<>();

        String chatPrompt = promptLoader.getPrompt("general-chat", GENERAL_CHAT_SYSTEM_PROMPT);
        messages.add(DeepSeekChatMessage.builder().role("system").content(chatPrompt).build());

        if (history != null && !history.isEmpty()) {
            for (AIChatHistoryMessage msg : history) {
                String normalizedRole = normalizeRole(msg.getRole());
                if (normalizedRole != null) {
                    messages.add(DeepSeekChatMessage.builder()
                            .role(normalizedRole)
                            .content(msg.getContent())
                            .build());
                }
            }
        }

        String fullMessage = userMessage + attachmentContext;
        messages.add(DeepSeekChatMessage.builder().role("user").content(fullMessage).build());

        log.info("AI 流式对话 Prompt：\n{}", JSONUtil.toJsonStr(messages));

        DeepSeekChatRequest request = DeepSeekChatRequest.builder()
                .messages(messages)
                .temperature(0.7)
                .stream(true)
                .build();

        deepSeekService.chatStream(request, chunk -> {
            try {
                Map<String, Object> textEvent = new LinkedHashMap<>();
                textEvent.put("chunk", chunk);
                emitter.send(SseEmitter.event().name("text").data(textEvent));
                fullText.append(chunk);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return AIChatReplyVO.builder()
                .messageType("text")
                .text(fullText.toString())
                .data(null)
                .build();
    }

    private void sendStreamError(SseEmitter emitter, Throwable e) {
        try {
            Map<String, Object> errorEvent = new LinkedHashMap<>();
            errorEvent.put("code", 500);
            errorEvent.put("message", e.getMessage() != null ? e.getMessage() : "未知错误");
            emitter.send(SseEmitter.event().name("error").data(errorEvent));
        } catch (Exception ex) {
            log.warn("发送 SSE 错误事件失败", ex);
        } finally {
            emitter.completeWithError(e);
        }
    }

    // ======================== 冰箱创建向导方法 ========================

    /**
     * 冰箱类型选项（硬编码，value 对应 biz_fridge_type 表 ID）。
     */
    private static final List<Map<String, Object>> FRIDGE_TYPE_OPTIONS = List.of(
            Map.of("label", "单门冰箱", "value", 1),
            Map.of("label", "双门冰箱", "value", 2),
            Map.of("label", "三门冰箱", "value", 3),
            Map.of("label", "对开门冰箱", "value", 4),
            Map.of("label", "十字对开门", "value", 5),
            Map.of("label", "T型三门", "value", 6),
            Map.of("label", "法式多门冰箱", "value", 7),
            Map.of("label", "日式多门冰箱", "value", 8)
    );

    /**
     * 创建初始空的向导表单数据。
     */
    private static Map<String, Object> emptyWizardFormData() {
        Map<String, Object> formData = new LinkedHashMap<>();
        formData.put("name", "");
        formData.put("fridgeTypeId", null);
        formData.put("totalCapacity", null);
        formData.put("isDefault", false);
        formData.put("address", "");
        formData.put("remark", "");
        return formData;
    }

    /**
     * 处理冰箱创建向导的初始触发（用户说"帮我创建一个冰箱"等）。
     * <p>返回步骤0，要求用户输入冰箱名称。</p>
     */
    private AIChatReplyVO handleFridgeCreationWizardInit() {
        return buildWizardReply(0, emptyWizardFormData(), "好的，让我们开始创建冰箱吧！");
    }

    /**
     * 处理冰箱创建向导的流程推进（纯后端状态驱动，不调用 LLM）。
     * <p>根据 wizardContext 中的 currentStep 和 formData 推进步骤。</p>
     *
     * @param request AI 聊天请求（包含 wizardContext）
     * @return 向导卡片回复
     */
    private AIChatReplyVO handleFridgeCreationWizard(AIChatRequest request) {
        AIChatWizardContext ctx = request.getWizardContext();
        String userMessage = request.getMessage() != null ? request.getMessage().trim() : "";

        // 检查是否取消创建
        if (isCancelIntent(userMessage)) {
            return AIChatReplyVO.builder()
                    .messageType("text")
                    .text("已取消创建冰箱，有什么其他可以帮你的吗？")
                    .data(null)
                    .build();
        }

        Map<String, Object> formData = ctx != null && ctx.getFormData() != null
                ? new LinkedHashMap<>(ctx.getFormData())
                : emptyWizardFormData();

        int currentStep = ctx != null && ctx.getCurrentStep() != null ? ctx.getCurrentStep() : 0;

        // 步骤0：名称（必填）
        String name = getString(formData, "name");
        if (name == null || name.isBlank()) {
            if (userMessage.isBlank()) {
                return buildWizardReply(0, formData, "冰箱名称不能为空哦，请给它取个名字吧。");
            }
            formData.put("name", userMessage);
            return buildWizardReply(1, formData, "好的，接下来请选择冰箱类型。");
        }

        // 步骤1：类型（必填）
        if (currentStep <= 1) {
            Long fridgeTypeId = getLong(formData, "fridgeTypeId");
            if (fridgeTypeId == null) {
                return buildWizardReply(1, formData, "请选择冰箱类型。");
            }
            return buildWizardReply(2, formData, "好的，请输入冰箱总容量（可选）。");
        }

        // 步骤2：容量（选填）
        if (currentStep == 2) {
            if (isSkipIntent(userMessage)) {
                formData.put("totalCapacity", null);
                return buildWizardReply(3, formData, "好的，接下来设置默认选项。");
            }
            // 前端已更新 formData，直接推进；若未更新尝试从 message 解析兜底
            if (!formData.containsKey("totalCapacity") || formData.get("totalCapacity") == null) {
                Integer capacity = parseCapacity(userMessage);
                if (capacity != null) {
                    formData.put("totalCapacity", capacity);
                }
            }
            return buildWizardReply(3, formData, "好的，接下来设置默认选项。");
        }

        // 步骤3：默认（选填）
        if (currentStep == 3) {
            if (isSkipIntent(userMessage)) {
                formData.put("isDefault", false);
                return buildWizardReply(4, formData, "好的，接下来填写地址和备注。");
            }
            if (!formData.containsKey("isDefault")) {
                formData.put("isDefault", false);
            }
            return buildWizardReply(4, formData, "好的，接下来填写地址和备注。");
        }

        // 步骤4：地址与备注（选填）
        if (currentStep == 4) {
            if (isSkipIntent(userMessage)) {
                formData.put("address", "");
                formData.put("remark", "");
                return buildWizardReply(5, formData, "请确认以下信息，无误后点击确认创建：");
            }
            if (!formData.containsKey("address")) {
                formData.put("address", "");
            }
            if (!formData.containsKey("remark")) {
                formData.put("remark", "");
            }
            return buildWizardReply(5, formData, "请确认以下信息，无误后点击确认创建：");
        }

        // 步骤5：确认页（兜底保护）
        return AIChatReplyVO.builder()
                .messageType("text")
                .text("信息已填写完整，请点击确认创建按钮完成创建。如需修改，可以取消后重新开始。")
                .data(null)
                .build();
    }

    /**
     * 构建冰箱创建向导的标准回复结构（6 步）。
     *
     * @param currentStep 当前步骤索引（0-based）
     * @param formData    已收集的表单数据
     * @param text        展示给用户的引导文本
     * @return AI 回复 VO
     */
    private AIChatReplyVO buildWizardReply(int currentStep, Map<String, Object> formData, String text) {
        List<Map<String, Object>> steps = List.of(
                Map.of("title", "名称", "description", "请给你的冰箱起个名字"),
                Map.of("title", "类型", "description", "请选择冰箱的类型"),
                Map.of("title", "容量", "description", "请输入冰箱的总容量（升）"),
                Map.of("title", "默认", "description", "是否设为默认冰箱？"),
                Map.of("title", "信息", "description", "请填写地址和备注（选填）"),
                Map.of("title", "确认", "description", "请确认以下信息")
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("currentStep", currentStep);
        data.put("totalSteps", 6);
        data.put("steps", steps);
        data.put("formData", formData);

        if (currentStep < 5) {
            Map<String, Object> currentInput = new LinkedHashMap<>();
            switch (currentStep) {
                case 0 -> {
                    currentInput.put("field", "name");
                    currentInput.put("label", "冰箱名称");
                    currentInput.put("placeholder", "如：家用冰箱、办公室冰箱");
                    currentInput.put("required", true);
                    currentInput.put("type", "text");
                }
                case 1 -> {
                    currentInput.put("field", "fridgeTypeId");
                    currentInput.put("label", "冰箱类型");
                    currentInput.put("placeholder", "请选择冰箱类型");
                    currentInput.put("required", true);
                    currentInput.put("type", "select");
                    currentInput.put("options", FRIDGE_TYPE_OPTIONS);
                }
                case 2 -> {
                    currentInput.put("field", "totalCapacity");
                    currentInput.put("label", "总容量");
                    currentInput.put("placeholder", "请输入容量（升）");
                    currentInput.put("required", false);
                    currentInput.put("type", "number");
                }
                case 3 -> {
                    currentInput.put("field", "isDefault");
                    currentInput.put("label", "设为默认冰箱");
                    currentInput.put("required", false);
                    currentInput.put("type", "switch");
                }
                case 4 -> {
                    currentInput.put("field", "address");
                    currentInput.put("label", "地址");
                    currentInput.put("placeholder", "请输入地址（选填）");
                    currentInput.put("required", false);
                    currentInput.put("type", "text");
                }
            }
            data.put("currentInput", currentInput);
        }

        return AIChatReplyVO.builder()
                .messageType("fridge_creation_wizard")
                .text(text)
                .data(data)
                .build();
    }

    /**
     * 安全获取表单中的字符串值。
     */
    private String getString(Map<String, Object> formData, String key) {
        Object value = formData.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * 安全获取表单中的 Long 值。
     */
    private Long getLong(Map<String, Object> formData, String key) {
        Object value = formData.get(key);
        if (value instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    /**
     * 从用户输入中解析容量数值。
     */
    private Integer parseCapacity(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        // 提取纯数字部分
        String digits = message.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 判断用户输入是否为"跳过"意图。
     */
    private boolean isSkipIntent(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String lower = message.trim().toLowerCase();
        return lower.contains("跳过") || lower.contains("skip")
                || lower.contains("不用填") || lower.contains("不填")
                || lower.contains("没有") || lower.contains("无")
                || lower.contains("略过") || lower.contains("pass")
                || lower.contains("不需要") || lower.contains("算了");
    }

    /**
     * 判断用户输入是否为"取消"意图。
     */
    private boolean isCancelIntent(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String lower = message.trim().toLowerCase();
        return lower.contains("取消") || lower.contains("cancel")
                || lower.contains("不创建了") || lower.contains("不建了")
                || lower.contains("放弃") || lower.contains("退出")
                || lower.contains("结束") || lower.contains("停止");
    }

    /**
     * 新鲜度状态记录。
     */
    private record FreshnessStatus(String label, String type, Integer remainingDays) {
    }
}
