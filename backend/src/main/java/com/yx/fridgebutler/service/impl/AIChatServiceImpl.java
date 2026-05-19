package com.yx.fridgebutler.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yx.fridgebutler.dto.aichat.AIChatHistoryMessage;
import com.yx.fridgebutler.dto.aichat.AIChatRequest;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatMessage;
import com.yx.fridgebutler.dto.item.ItemSearchRequest;
import com.yx.fridgebutler.entity.AiChatMessage;
import com.yx.fridgebutler.entity.AiChatSession;
import com.yx.fridgebutler.entity.BizFridge;
import com.yx.fridgebutler.entity.BizFridgeItem;
import com.yx.fridgebutler.entity.BizItemCategory;
import com.yx.fridgebutler.entity.BizItemUnit;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.AiChatMessageRepository;
import com.yx.fridgebutler.repository.AiChatSessionRepository;
import com.yx.fridgebutler.repository.BizFridgeItemRepository;
import com.yx.fridgebutler.repository.BizFridgeRepository;
import com.yx.fridgebutler.repository.BizItemCategoryRepository;
import com.yx.fridgebutler.repository.BizItemUnitRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.AIChatService;
import com.yx.fridgebutler.service.DeepSeekService;
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
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
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
            你是一个冰箱管理助手的意图识别系统。请严格分析用户输入，返回纯JSON，不要包含任何其他文字（包括markdown代码块标记、解释说明等）。

            支持的意图类型：
            - fridge_list: 查看冰箱列表，如"我的冰箱有哪些"
            - item_list: 查看物品/库存/食材列表，可能包含关键词和冰箱名称，如"冰箱里还有什么鸡蛋"
            - expiring_alert: 查看临期/过期提醒，如"有什么快过期的"
            - recipe_recommend: 根据库存推荐菜谱，如"今天吃什么""推荐菜谱"
            - trend_chart: 查看趋势/统计图表，如"近7天取出趋势"
            - action_confirm: 删除/清空/移除等需要确认的操作，如"删除厨房冰箱"
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
            - text: 无参数，params为空对象{}

            注意事项：
            1. 如果用户输入与冰箱管理完全无关（如"今天天气怎么样"），返回 text
            2. 如果用户意图不明确或含糊，返回 text
            3. 必须只返回JSON字符串，不要添加```json标记
            """;

    private static final String RECIPE_SYSTEM_PROMPT = """
            你是一位擅长家常菜的厨师。根据用户提供的冰箱库存食材，推荐2-3道适合的家常菜。

            要求：
            1. 优先推荐用户库存食材能满足大部分需求的菜
            2. 每道菜包含：名称、难度（简单/中等/困难）、预计烹饪时间、已匹配的库存食材列表、缺少的食材列表（如有）、简短描述
            3. 返回严格JSON格式，不要包含任何其他文字（包括markdown代码块标记）：

            {"recipes":[{"name":"菜名","difficulty":"简单","cookTime":"10分钟","matchedItems":["食材1","食材2"],"missingItems":["食材3"],"description":"描述"}],"text":"根据你的库存，为你推荐以下x道菜："}
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
    private BizItemCategoryRepository categoryRepository;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private AiChatSessionRepository sessionRepository;

    @Autowired
    private AiChatMessageRepository messageRepository;

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
                isNewSession = true;
                session = createNewSession(sessionId, currentUserId, request.getMessage());
            }
        } else {
            session = createNewSession(sessionId, currentUserId, request.getMessage());
        }

        // 2. 从数据库加载历史（优先于前端传的 history，支持跨页面刷新）
        List<AIChatHistoryMessage> dbHistory = buildHistoryFromDb(sessionId);

        // 3. 保存用户消息
        saveUserMessage(sessionId, request.getMessage());

        // 4. 意图识别（使用数据库历史）
        IntentResult intent = recognizeIntent(request.getMessage(), dbHistory);
        log.info("AI 意图识别结果：intent={}, params={}, confidence={}",
                intent.intent, intent.params, intent.confidence);

        // 5. 根据意图分发处理
        AIChatReplyVO reply;
        try {
            reply = switch (intent.intent) {
                case "fridge_list" -> handleFridgeList();
                case "item_list" -> handleItemList(intent.params);
                case "expiring_alert" -> handleExpiringAlert();
                case "recipe_recommend" -> handleRecipeRecommend();
                case "trend_chart" -> handleTrendChart(intent.params);
                case "action_confirm" -> handleActionConfirm(intent.params);
                default -> handleText(request.getMessage(), dbHistory);
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
     * @param userMessage 用户当前输入
     * @param history     对话历史
     * @return 意图识别结果
     */
    private IntentResult recognizeIntent(String userMessage, List<AIChatHistoryMessage> history) {
        List<DeepSeekChatMessage> messages = new ArrayList<>();
        messages.add(DeepSeekChatMessage.builder().role("system").content(INTENT_SYSTEM_PROMPT).build());

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

        messages.add(DeepSeekChatMessage.builder().role("user").content(userMessage).build());

        String response = deepSeekService.chat(messages);
        return parseIntentJson(response);
    }

    /**
     * 解析 DeepSeek 返回的意图 JSON。
     */
    private IntentResult parseIntentJson(String jsonStr) {
        String cleaned = cleanJsonResponse(jsonStr);
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

    /**
     * 清理 DeepSeek 可能返回的 markdown 代码块等包装。
     */
    private String cleanJsonResponse(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline != -1) {
                cleaned = cleaned.substring(firstNewline + 1);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.lastIndexOf("```")).trim();
            }
        }
        return cleaned;
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
     * <p>获取用户库存食材列表，交由 DeepSeek 生成推荐菜谱。</p>
     */
    private AIChatReplyVO handleRecipeRecommend() {
        Long currentUserId = getCurrentUserId();

        // 获取所有库存物品名称（去重）
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

        String inventoryText = inventoryItems.isEmpty()
                ? "用户冰箱目前没有食材。"
                : "用户冰箱中的食材有：" + String.join("、", inventoryItems) + "。";

        String userPrompt = inventoryText + "请根据这些食材推荐2-3道适合的家常菜。如果食材很少或没有，也给出合理建议。";

        String response = deepSeekService.chat(RECIPE_SYSTEM_PROMPT, userPrompt);
        String cleaned = cleanJsonResponse(response);

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
    private AIChatReplyVO handleText(String userMessage, List<AIChatHistoryMessage> history) {
        List<DeepSeekChatMessage> messages = new ArrayList<>();

        String systemPrompt = "你是冰箱管家，一个智能冰箱管理助手。你可以帮助用户查询冰箱库存、查看临期提醒、推荐菜谱。回答要简洁友好，不要太长。";
        messages.add(DeepSeekChatMessage.builder().role("system").content(systemPrompt).build());

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

        messages.add(DeepSeekChatMessage.builder().role("user").content(userMessage).build());

        String response = deepSeekService.chat(messages);

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
     * 保存用户消息到数据库。
     */
    private void saveUserMessage(String sessionId, String content) {
        AiChatMessage msg = AiChatMessage.builder()
                .sessionId(sessionId)
                .role("user")
                .content(content)
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
     */
    private List<AIChatHistoryMessage> buildHistoryFromDb(String sessionId) {
        List<AiChatMessage> messages = messageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
        return messages.stream()
                .map(m -> AIChatHistoryMessage.builder()
                        .role(m.getRole())
                        .content(m.getContent() != null ? m.getContent() : "")
                        .build())
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
            default -> List.of("查看冰箱", "临期提醒", "推荐菜谱");
        };
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
            for (int i = 0; i < array.size(); i++) {
                Object item = array.get(i);
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
    private static class IntentResult {
        final String intent;
        final Map<String, Object> params;
        final double confidence;

        IntentResult(String intent, Map<String, Object> params, double confidence) {
            this.intent = intent != null ? intent : "text";
            this.params = params != null ? params : new HashMap<>();
            this.confidence = confidence;
        }
    }

    /**
     * 新鲜度状态记录。
     */
    private record FreshnessStatus(String label, String type, Integer remainingDays) {
    }
}
