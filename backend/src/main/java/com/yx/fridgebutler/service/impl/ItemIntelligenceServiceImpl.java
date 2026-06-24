package com.yx.fridgebutler.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yx.fridgebutler.config.PromptTemplateLoader;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatMessage;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatRequest;
import com.yx.fridgebutler.dto.item.ItemRecommendationResult;
import com.yx.fridgebutler.entity.BizItemCategory;
import com.yx.fridgebutler.entity.BizItemUnit;
import com.yx.fridgebutler.entity.BizUnitType;
import com.yx.fridgebutler.repository.BizItemCategoryRepository;
import com.yx.fridgebutler.repository.BizItemUnitRepository;
import com.yx.fridgebutler.repository.BizUnitTypeRepository;
import com.yx.fridgebutler.service.DeepSeekService;
import com.yx.fridgebutler.service.ItemIntelligenceService;
import com.yx.fridgebutler.util.AiResponseUtils;
import com.yx.fridgebutler.util.UserContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 物品智能推荐服务实现类。
 * <p>调用 DeepSeek AI，结合系统预设及用户自定义的分类、单位数据，为用户输入的物品名称生成推荐。</p>
 */
@Slf4j
@Service
public class ItemIntelligenceServiceImpl implements ItemIntelligenceService {

    /** Prompt 模板 key。 */
    private static final String PROMPT_KEY = "item-recommendation";

    /** 当外部 prompt 模板缺失时的兜底提示词。 */
    private static final String FALLBACK_PROMPT = """
            你是一台智能冰箱助手。请根据用户输入的物品名称，判断它是否是真实、可放入冰箱保存的物品。

            可选分类（id: 名称）：
            {{categories}}

            可选单位（id: 名称, 单位类型）：
            {{units}}

            请从上述列表中选择最合适的 categoryId 和 unitId，没有合适则返回 null。
            自由推荐 storageLocation（如冷藏室、冷冻室），没有则返回 null。
            storedDate 格式 yyyy-MM-dd，默认今天。

            输出严格 JSON：
            {"valid":true,"itemName":"苹果","categoryId":1,"unitId":1,"storageLocation":"冷藏室","storedDate":"2026-06-15","message":null}
            """;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private PromptTemplateLoader promptLoader;

    @Autowired
    private BizItemCategoryRepository categoryRepository;

    @Autowired
    private BizItemUnitRepository unitRepository;

    @Autowired
    private BizUnitTypeRepository unitTypeRepository;

    @Override
    public ItemRecommendationResult recommend(String itemName, Long fridgeId) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        log.info("AI 推荐物品，用户ID：{}，冰箱ID：{}，物品名称：{}", currentUserId, fridgeId, itemName);

        // 查询当前用户可用的分类和单位
        List<BizItemCategory> categories = categoryRepository.findAllByOwnerIdOrSystemDefault(currentUserId);
        List<BizItemUnit> units = unitRepository.findAllByOwnerIdOrSystemDefault(currentUserId);

        // 批量查询单位类型，用于展示
        List<Long> unitTypeIds = units.stream()
                .map(BizItemUnit::getUnitTypeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, BizUnitType> unitTypeMap = unitTypeRepository.findAllById(unitTypeIds).stream()
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .collect(Collectors.toMap(BizUnitType::getId, Function.identity()));

        // 构建 ID 到名称的映射，用于校验 AI 返回的 ID 是否有效
        Map<Long, String> validCategoryMap = categories.stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .collect(Collectors.toMap(BizItemCategory::getId, BizItemCategory::getCategoryName));
        Map<Long, BizItemUnit> validUnitMap = units.stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .collect(Collectors.toMap(BizItemUnit::getId, Function.identity()));

        try {
            String systemPrompt = buildSystemPrompt(categories, units, unitTypeMap);
            List<DeepSeekChatMessage> messages = List.of(
                    DeepSeekChatMessage.builder().role("system").content(systemPrompt).build(),
                    DeepSeekChatMessage.builder().role("user").content(itemName).build()
            );

            DeepSeekChatRequest request = DeepSeekChatRequest.builder()
                    .messages(messages)
                    .temperature(0.1)
                    .responseFormat(Map.of("type", "json_object"))
                    .build();

            String response = deepSeekService.chat(request);
            log.debug("AI 推荐原始响应：{}", response);
            return parseRecommendationResponse(response, itemName, validCategoryMap, validUnitMap, unitTypeMap);
        } catch (Exception e) {
            log.warn("AI 推荐物品失败，降级返回空推荐，物品名称：{}", itemName, e);
            return invalidResult(itemName, "AI 推荐服务暂不可用，请手动填写");
        }
    }

    /**
     * 构建系统提示词，将分类和单位列表注入模板。
     */
    private String buildSystemPrompt(List<BizItemCategory> categories,
                                     List<BizItemUnit> units,
                                     Map<Long, BizUnitType> unitTypeMap) {
        String template = promptLoader.getPrompt(PROMPT_KEY, FALLBACK_PROMPT);

        String categoriesText = categories.stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .map(c -> c.getId() + ": " + c.getCategoryName())
                .collect(Collectors.joining("\n"));
        if (categoriesText.isBlank()) {
            categoriesText = "（暂无可用分类）";
        }

        String unitsText = units.stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .map(u -> {
                    String typeName = unitTypeMap.getOrDefault(u.getUnitTypeId(), new BizUnitType()).getUnitTypeName();
                    return u.getId() + ": " + u.getUnitName() + " (" + typeName + ")";
                })
                .collect(Collectors.joining("\n"));
        if (unitsText.isBlank()) {
            unitsText = "（暂无可用单位）";
        }

        return template
                .replace("{{categories}}", categoriesText)
                .replace("{{units}}", unitsText)
                .replace("{{today}}", LocalDate.now().toString());
    }

    /**
     * 解析 AI 返回的 JSON 响应，并校验 ID 是否存在于提供的列表中。
     */
    private ItemRecommendationResult parseRecommendationResponse(String response,
                                                                  String originalItemName,
                                                                  Map<Long, String> validCategoryMap,
                                                                  Map<Long, BizItemUnit> validUnitMap,
                                                                  Map<Long, BizUnitType> unitTypeMap) {
        String cleaned = AiResponseUtils.cleanJsonResponse(response);
        if (cleaned.isBlank()) {
            log.warn("AI 推荐返回空响应，物品名称：{}", originalItemName);
            return invalidResult(originalItemName, "AI 推荐返回为空，请手动填写");
        }

        try {
            JSONObject root = JSONUtil.parseObj(cleaned);
            boolean valid = root.getBool("valid", false);
            if (!valid) {
                return invalidResult(originalItemName, root.getStr("message", "该物品不适合放入冰箱保存"));
            }

            String recommendedItemName = root.getStr("itemName", originalItemName);
            LocalDate storedDate = parseStoredDate(root.getStr("storedDate"));
            String storageLocation = trimToNull(root.getStr("storageLocation"));

            // 校验分类 ID
            Long categoryId = root.getLong("categoryId");
            String categoryName = null;
            if (categoryId != null && validCategoryMap.containsKey(categoryId)) {
                categoryName = validCategoryMap.get(categoryId);
            } else {
                categoryId = null;
            }

            // 校验单位 ID
            Long unitId = root.getLong("unitId");
            String unitName = null;
            String unitTypeName = null;
            if (unitId != null && validUnitMap.containsKey(unitId)) {
                BizItemUnit unit = validUnitMap.get(unitId);
                unitName = unit.getUnitName();
                BizUnitType unitType = unitTypeMap.get(unit.getUnitTypeId());
                unitTypeName = unitType != null ? unitType.getUnitTypeName() : null;
            } else {
                unitId = null;
            }

            return ItemRecommendationResult.builder()
                    .valid(true)
                    .itemName(recommendedItemName)
                    .categoryId(categoryId)
                    .categoryName(categoryName)
                    .unitId(unitId)
                    .unitName(unitName)
                    .unitTypeName(unitTypeName)
                    .storageLocation(storageLocation)
                    .storedDate(storedDate)
                    .message(null)
                    .build();
        } catch (Exception e) {
            log.warn("AI 推荐响应解析失败，物品名称：{}，响应：{}", originalItemName, cleaned, e);
            return invalidResult(originalItemName, "AI 推荐解析失败，请手动填写");
        }
    }

    /**
     * 解析存放日期，失败或为空时返回今天。
     */
    private LocalDate parseStoredDate(String storedDateStr) {
        if (storedDateStr == null || storedDateStr.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(storedDateStr.trim());
        } catch (Exception e) {
            log.debug("存放日期解析失败：{}，使用今天", storedDateStr);
            return LocalDate.now();
        }
    }

    /**
     * 去除字符串首尾空白，空字符串返回 null。
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 构造无效推荐结果。
     */
    private ItemRecommendationResult invalidResult(String originalItemName, String message) {
        return ItemRecommendationResult.builder()
                .valid(false)
                .itemName(originalItemName)
                .categoryId(null)
                .categoryName(null)
                .unitId(null)
                .unitName(null)
                .unitTypeName(null)
                .storageLocation(null)
                .storedDate(LocalDate.now())
                .message(message)
                .build();
    }

}
