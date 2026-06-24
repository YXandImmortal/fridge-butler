package com.yx.fridgebutler.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yx.fridgebutler.config.PromptTemplateLoader;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatMessage;
import com.yx.fridgebutler.dto.deepseek.DeepSeekChatRequest;
import com.yx.fridgebutler.dto.purchase.PurchaseRecommendRequest;
import com.yx.fridgebutler.dto.purchase.StorageLocationSuggestRequest;
import com.yx.fridgebutler.entity.*;
import com.yx.fridgebutler.enums.SceneTemplate;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.*;
import com.yx.fridgebutler.service.CapacityStatsService;
import com.yx.fridgebutler.service.DeepSeekService;
import com.yx.fridgebutler.service.PurchaseIntelligenceService;
import com.yx.fridgebutler.util.UserContextUtil;
import com.yx.fridgebutler.vo.fridge.CapacityStatsVO;
import com.yx.fridgebutler.vo.fridge.FridgeCapacityRateVO;
import com.yx.fridgebutler.vo.purchase.PurchaseRecommendItemVO;
import com.yx.fridgebutler.vo.purchase.PurchaseRecommendVO;
import com.yx.fridgebutler.vo.purchase.SceneTemplateVO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 采购智能推荐服务实现。
 */
@Slf4j
@Service
public class PurchaseIntelligenceServiceImpl implements PurchaseIntelligenceService {

    /** 日常推荐数据充足性阈值。 */
    private static final int DATA_SUFFICIENCY_THRESHOLD = 3;

    /** 日常推荐 Prompt 模板 key。 */
    private static final String DAILY_PROMPT_KEY = "purchase-daily-recommend";

    /** 特殊场景生成 Prompt 模板 key。 */
    private static final String SPECIAL_PROMPT_KEY = "purchase-special-generate";

    /** 存放位置推荐 Prompt 模板 key。 */
    private static final String STORAGE_LOCATION_PROMPT_KEY = "purchase-settle-storage-location";

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private PromptTemplateLoader promptLoader;

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private BizFridgeItemRepository itemRepository;

    @Autowired
    private BizItemAddRecordRepository addRecordRepository;

    @Autowired
    private BizItemTakeOutRecordRepository takeOutRecordRepository;

    @Autowired
    private BizItemCategoryRepository categoryRepository;

    @Autowired
    private BizItemUnitRepository unitRepository;

    @Autowired
    private BizFridgeCapacityRateRepository capacityRateRepository;

    @Autowired
    private CapacityStatsService capacityStatsService;

    @Autowired
    private BizUnitTypeRepository unitTypeRepository;

    @Override
    public PurchaseRecommendVO recommend(PurchaseRecommendRequest request) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        Long fridgeId = request.getFridgeId();

        // 校验冰箱归属
        BizFridge fridge = fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(fridgeId, currentUserId)
                .orElseThrow(BusinessException::fridgeNotFound);

        String mode = request.getMode();
        if ("daily".equalsIgnoreCase(mode)) {
            return recommendDaily(currentUserId, fridge);
        } else if ("special".equalsIgnoreCase(mode)) {
            return generateSpecial(currentUserId, fridge, request);
        } else {
            throw new BusinessException(400, org.springframework.http.HttpStatus.BAD_REQUEST, "不支持的推荐模式：" + mode);
        }
    }

    @Override
    public List<String> suggestStorageLocations(List<StorageLocationSuggestRequest> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        String prompt = buildStorageLocationPrompt(items);

        try {
            List<DeepSeekChatMessage> messages = List.of(
                    DeepSeekChatMessage.builder().role("system").content(prompt).build(),
                    DeepSeekChatMessage.builder().role("user").content("请推荐存放位置。").build()
            );

            DeepSeekChatRequest request = DeepSeekChatRequest.builder()
                    .messages(messages)
                    .temperature(0.3)
                    .responseFormat(Map.of("type", "json_object"))
                    .build();

            String response = deepSeekService.chat(request);
            return parseStorageLocationResponse(response, items.size());
        } catch (Exception e) {
            log.warn("AI 存放位置推荐失败，物品数：{}", items.size(), e);
            return new ArrayList<>(Collections.nCopies(items.size(), null));
        }
    }

    /**
     * 构建存放位置推荐 Prompt。
     */
    private String buildStorageLocationPrompt(List<StorageLocationSuggestRequest> items) {
        String template = promptLoader.getPrompt(STORAGE_LOCATION_PROMPT_KEY, getStorageLocationFallbackPrompt());
        return template.replace("{{items}}", formatStorageLocationItems(items));
    }

    /**
     * 格式化待推荐物品列表。
     */
    private String formatStorageLocationItems(List<StorageLocationSuggestRequest> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            StorageLocationSuggestRequest item = items.get(i);
            sb.append(i).append(". ").append(item.getItemName());
            boolean hasCategory = item.getCategoryName() != null && !item.getCategoryName().isBlank();
            boolean hasShelfLife = item.getShelfLifeDays() != null;
            if (hasCategory || hasShelfLife) {
                sb.append("（");
                if (hasCategory) {
                    sb.append("分类：").append(item.getCategoryName());
                }
                if (hasCategory && hasShelfLife) {
                    sb.append("，");
                }
                if (hasShelfLife) {
                    sb.append("保质期：").append(item.getShelfLifeDays()).append("天");
                }
                sb.append("）");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 解析 AI 存放位置推荐响应。
     */
    private List<String> parseStorageLocationResponse(String response, int itemCount) {
        List<String> result = new ArrayList<>(Collections.nCopies(itemCount, null));
        if (response == null || response.isBlank()) {
            return result;
        }

        String cleaned = cleanJsonResponse(response);
        if (cleaned.isBlank()) {
            return result;
        }

        try {
            JSONObject root = JSONUtil.parseObj(cleaned);
            JSONArray locations = root.getJSONArray("locations");
            if (locations == null) {
                return result;
            }

            for (int i = 0; i < locations.size(); i++) {
                JSONObject obj = locations.getJSONObject(i);
                if (obj == null) {
                    continue;
                }
                Integer index = obj.getInt("index");
                String storageLocation = obj.getStr("storageLocation");
                if (index != null && index >= 0 && index < itemCount
                        && storageLocation != null && !storageLocation.isBlank()) {
                    result.set(index, storageLocation);
                }
            }
        } catch (Exception e) {
            log.warn("AI 存放位置响应解析失败，响应：{}", response, e);
        }
        return result;
    }

    @Override
    public List<SceneTemplateVO> listSceneTemplates() {
        return Arrays.stream(SceneTemplate.values())
                .map(t -> SceneTemplateVO.builder()
                        .code(t.getCode())
                        .name(t.getName())
                        .build())
                .toList();
    }

    /**
     * 日常采购推荐。
     */
    private PurchaseRecommendVO recommendDaily(Long userId, BizFridge fridge) {
        log.info("日常采购推荐，用户ID：{}，冰箱ID：{}", userId, fridge.getId());

        // 1. 数据充足性预检
        DataSummary summary = aggregateData(userId, fridge);
        if (!summary.isSufficient()) {
            return PurchaseRecommendVO.builder()
                    .sufficientData(false)
                    .insufficientReason("当前冰箱数据较少，AI 暂时无法给出可靠的日常推荐。建议先多录入一些食材，或手动创建采购计划。")
                    .tips(List.of())
                    .items(List.of())
                    .build();
        }

        // 2. 加载模板并注入变量
        String prompt = buildDailyPrompt(fridge, summary);

        // 3. 调用 AI
        try {
            List<DeepSeekChatMessage> messages = List.of(
                    DeepSeekChatMessage.builder().role("system").content(prompt).build(),
                    DeepSeekChatMessage.builder().role("user").content("请推荐本次日常采购清单。").build()
            );

            DeepSeekChatRequest request = DeepSeekChatRequest.builder()
                    .messages(messages)
                    .temperature(0.3)
                    .responseFormat(Map.of("type", "json_object"))
                    .build();

            String response = deepSeekService.chat(request);
            return parseRecommendResponse(response, summary.getValidCategoryMap(), summary.getValidUnitMap(), summary.getUnitTypeMap());
        } catch (Exception e) {
            log.warn("日常采购推荐失败，降级返回空推荐，用户ID：{}，冰箱ID：{}", userId, fridge.getId(), e);
            return PurchaseRecommendVO.builder()
                    .sufficientData(false)
                    .insufficientReason("AI 推荐服务暂不可用，请稍后再试或手动创建计划。")
                    .tips(List.of())
                    .items(List.of())
                    .build();
        }
    }

    /**
     * 特殊场景采购生成。
     */
    private PurchaseRecommendVO generateSpecial(Long userId, BizFridge fridge, PurchaseRecommendRequest request) {
        // 0. 特殊场景人数校验
        if (request.getEstimatedPeople() == null || request.getEstimatedPeople() <= 0) {
            throw new BusinessException(400, org.springframework.http.HttpStatus.BAD_REQUEST,
                    "特殊场景模式下 estimatedPeople 必须大于 0");
        }

        // 1. 确定场景描述
        String sceneDesc = request.getSceneDesc();
        SceneTemplate sceneTemplate = null;
        if (request.getSceneTemplate() != null && !request.getSceneTemplate().isBlank()) {
            sceneTemplate = SceneTemplate.fromCode(request.getSceneTemplate());
        }
        if (sceneTemplate != null) {
            String templateDesc = loadSceneDesc(sceneTemplate);
            if (sceneDesc == null || sceneDesc.isBlank()) {
                sceneDesc = templateDesc;
            } else {
                sceneDesc = templateDesc + "；用户补充：" + sceneDesc;
            }
        }
        if (sceneDesc == null || sceneDesc.isBlank()) {
            throw new BusinessException(400, org.springframework.http.HttpStatus.BAD_REQUEST, "场景描述不能为空");
        }

        // 提前解析场景描述中的人数占位符，便于日志与后续处理显示真实值
        sceneDesc = sceneDesc.replace("{{estimatedPeople}}", String.valueOf(request.getEstimatedPeople()));

        log.info("特殊场景采购生成，用户ID：{}，冰箱ID：{}，场景：{}", userId, fridge.getId(), sceneDesc);

        // 2. 聚合基础数据
        DataSummary summary = aggregateBasicData(fridge);

        // 3. 加载模板并注入变量
        String prompt = buildSpecialPrompt(fridge, sceneDesc, request.getEstimatedPeople(), summary);
        log.debug("特殊场景生成 Prompt：\n{}", prompt);

        // 4. 调用 AI
        try {
            List<DeepSeekChatMessage> messages = List.of(
                    DeepSeekChatMessage.builder().role("system").content(prompt).build(),
                    DeepSeekChatMessage.builder().role("user").content("请生成采购清单。").build()
            );

            DeepSeekChatRequest chatRequest = DeepSeekChatRequest.builder()
                    .messages(messages)
                    .temperature(0.3)
                    .responseFormat(Map.of("type", "json_object"))
                    .build();

            String response = deepSeekService.chat(chatRequest);
            PurchaseRecommendVO result = parseRecommendResponse(response,
                    summary.getValidCategoryMap(), summary.getValidUnitMap(), summary.getUnitTypeMap());
            // 特殊场景默认数据充足
            result.setSufficientData(true);
            return result;
        } catch (Exception e) {
            log.warn("特殊场景采购生成失败，降级返回空推荐，用户ID：{}，冰箱ID：{}", userId, fridge.getId(), e);
            return PurchaseRecommendVO.builder()
                    .sufficientData(false)
                    .insufficientReason("AI 生成服务暂不可用，请稍后再试或手动创建计划。")
                    .tips(List.of())
                    .items(List.of())
                    .build();
        }
    }

    /**
     * 聚合日常推荐所需数据。
     */
    private DataSummary aggregateData(Long userId, BizFridge fridge) {
        DataSummary summary = aggregateBasicData(fridge);

        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);

        // 近30天入库记录
        List<BizItemAddRecord> addRecords = addRecordRepository.findByFridgeIdAndCreateTimeAfter(fridge.getId(), thirtyDaysAgo);
        summary.setAddRecords(addRecords);

        // 近30天取出记录
        List<BizItemTakeOutRecord> takeOutRecords = takeOutRecordRepository.findByFridgeIdAndCreateTimeAfter(fridge.getId(), thirtyDaysAgo);
        summary.setTakeOutRecords(takeOutRecords);

        // 高频消耗物品 Top 10
        Map<String, Long> takeOutFrequency = takeOutRecords.stream()
                .collect(Collectors.groupingBy(BizItemTakeOutRecord::getItemName, Collectors.counting()));
        List<String> frequentItems = takeOutFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();
        summary.setFrequentItems(frequentItems);

        return summary;
    }

    /**
     * 聚合基础数据（库存、分类、单位、容量等）。
     */
    private DataSummary aggregateBasicData(BizFridge fridge) {
        DataSummary summary = new DataSummary();

        // 当前库存
        List<BizFridgeItem> items = itemRepository.findByFridgeIdAndIsDeletedFalse(fridge.getId());
        summary.setItems(items);

        // 容量利用率
        CapacityStatsVO capacityStats = capacityStatsService.getCapacityStats(fridge.getId());
        boolean hasCapacityStats = capacityStats != null
                && capacityStats.getFridgeRates() != null
                && !capacityStats.getFridgeRates().isEmpty();
        if (hasCapacityStats) {
            FridgeCapacityRateVO rateVo = capacityStats.getFridgeRates().getFirst();
            summary.setCapacityRate(rateVo.getRate());
            summary.setItemCount(rateVo.getItemCount());
        } else {
            summary.setCapacityRate(0);
            summary.setItemCount(items.size());
        }
        summary.setCapacityStatsAvailable(hasCapacityStats);
        summary.setTotalCapacity(fridge.getTotalCapacity());

        // 分类和单位：AI 推荐只使用系统默认分类/单位，避免用户测试数据干扰 AI 判断
        List<BizItemCategory> categories = categoryRepository.findByIsSystemDefaultTrueAndIsDeletedFalse();
        List<BizItemUnit> units = unitRepository.findByIsSystemDefaultTrueAndIsDeletedFalse();

        Map<Long, String> categoryMap = categories.stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .collect(Collectors.toMap(BizItemCategory::getId, BizItemCategory::getCategoryName));
        Map<Long, BizItemUnit> unitMap = units.stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .collect(Collectors.toMap(BizItemUnit::getId, Function.identity()));

        Set<Long> unitTypeIds = units.stream()
                .map(BizItemUnit::getUnitTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> unitTypeMap = unitTypeRepository.findAllById(unitTypeIds).stream()
                .filter(type -> !Boolean.TRUE.equals(type.getIsDeleted()))
                .collect(Collectors.toMap(BizUnitType::getId, BizUnitType::getUnitTypeName));

        summary.setValidCategoryMap(categoryMap);
        summary.setValidUnitMap(unitMap);
        summary.setUnitTypeMap(unitTypeMap);

        return summary;
    }

    /**
     * 构建日常推荐 Prompt。
     */
    private String buildDailyPrompt(BizFridge fridge, DataSummary summary) {
        String template = promptLoader.getPrompt(DAILY_PROMPT_KEY, getDailyFallbackPrompt());

        return template
                .replace("{{today}}", LocalDate.now().toString())
                .replace("{{fridgeName}}", fridge.getFridgeName())
                .replace("{{capacityRate}}", String.valueOf(summary.getCapacityRate()))
                .replace("{{totalCapacity}}", String.valueOf(summary.getTotalCapacity()))
                .replace("{{inventoryList}}", formatInventoryList(summary.getItems(), summary.getValidUnitMap()))
                .replace("{{addRecords}}", formatAddRecords(summary.getAddRecords()))
                .replace("{{takeOutRecords}}", formatTakeOutRecords(summary.getTakeOutRecords()))
                .replace("{{frequentItems}}", String.join("、", summary.getFrequentItems()))
                .replace("{{categories}}", formatCategories(summary.getValidCategoryMap()))
                .replace("{{units}}", formatUnits(summary.getValidUnitMap(), summary.getUnitTypeMap()));
    }

    /**
     * 构造特殊场景下的冰箱容量提示语。
     * <p>根据总容量和容量利用率的可用性，给出不同的约束说明：</p>
     * <ul>
     *     <li>两者都缺失：无需考虑容量限制。</li>
     *     <li>仅总容量缺失：参考利用率，但不受总容量上限约束。</li>
     *     <li>仅利用率缺失：按总容量给出粗略上限提示。</li>
     *     <li>两者都有：给出 85%/95% 阈值建议。</li>
     * </ul>
     */
    private String buildCapacityHint(BizFridge fridge, DataSummary summary) {
        boolean totalCapacityAvailable = summary.getTotalCapacity() != null;
        boolean rateAvailable = summary.isCapacityStatsAvailable();

        if (!totalCapacityAvailable && !rateAvailable) {
            return "冰箱容量信息未维护（总容量未设置、容量利用率未统计），本场景推荐无需考虑容量限制。";
        }

        if (!totalCapacityAvailable) {
            return String.format("总容量未设置，当前容量利用率为 %d%%。推荐时无需严格受限于总容量，但可适当参考当前利用率，避免过量采购。",
                    summary.getCapacityRate());
        }

        if (!rateAvailable) {
            return String.format("冰箱总容量为 %d 升，容量利用率未统计。推荐时可按场景需求采购，但总容量为 %d 升，请避免明显超出。",
                    summary.getTotalCapacity(), summary.getTotalCapacity());
        }

        return String.format("冰箱总容量为 %d 升，当前容量利用率为 %d%%。若利用率超过 85%%，建议减少新增或优先消耗库存；若超过 95%%，请在 tips 中增加\"冰箱容量紧张，建议优先消耗库存\"。",
                summary.getTotalCapacity(), summary.getCapacityRate());
    }

    /**
     * 构建特殊场景生成 Prompt。
     */
    private String buildSpecialPrompt(BizFridge fridge, String sceneDesc, Integer estimatedPeople, DataSummary summary) {
        String template = promptLoader.getPrompt(SPECIAL_PROMPT_KEY, getSpecialFallbackPrompt());

        // 场景文件中已自带单位（人/个宝宝），这里只替换数字
        String peopleText = String.valueOf(estimatedPeople);

        return template
                .replace("{{today}}", LocalDate.now().toString())
                .replace("{{sceneDesc}}", sceneDesc)
                .replace("{{fridgeName}}", fridge.getFridgeName())
                .replace("{{inventoryList}}", formatInventoryList(summary.getItems(), summary.getValidUnitMap()))
                .replace("{{estimatedPeople}}", peopleText)
                .replace("{{categories}}", formatCategories(summary.getValidCategoryMap()))
                .replace("{{units}}", formatUnits(summary.getValidUnitMap(), summary.getUnitTypeMap()))
                .replace("{{totalCapacity}}", summary.getTotalCapacity() != null ? summary.getTotalCapacity() + " 升" : "未设置")
                .replace("{{capacityRate}}", summary.isCapacityStatsAvailable() ? summary.getCapacityRate() + "%" : "未统计")
                .replace("{{capacityHint}}", buildCapacityHint(fridge, summary));
    }

    /**
     * 解析 tips 字段，兼容字符串与字符串数组。
     */
    private List<String> parseTips(Object tipsNode) {
        return switch (tipsNode) {
            case null -> List.of();
            case String s -> s.isBlank() ? List.of() : List.of(s);
            case JSONArray arr -> arr.toList(String.class).stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
            default -> List.of();
        };
    }

    /**
     * 解析 AI 推荐响应。
     */
    private PurchaseRecommendVO parseRecommendResponse(String response,
                                                       Map<Long, String> validCategoryMap,
                                                       Map<Long, BizItemUnit> validUnitMap,
                                                       Map<Long, String> unitTypeMap) {
        if (response == null || response.isBlank()) {
            return emptyResult();
        }

        String cleaned = cleanJsonResponse(response);
        if (cleaned.isBlank()) {
            return emptyResult();
        }

        try {
            JSONObject root = JSONUtil.parseObj(cleaned);

            Boolean sufficientData = root.getBool("sufficientData", true);
            String insufficientReason = root.getStr("insufficientReason");
            List<String> tips = parseTips(root.get("tips"));

            if (Boolean.FALSE.equals(sufficientData)) {
                return PurchaseRecommendVO.builder()
                        .sufficientData(false)
                        .insufficientReason(insufficientReason != null ? insufficientReason : "AI 认为当前数据不足")
                        .tips(tips)
                        .items(List.of())
                        .build();
            }

            JSONArray itemsArray = root.getJSONArray("items");
            if (itemsArray == null) {
                return PurchaseRecommendVO.builder()
                        .sufficientData(true)
                        .tips(tips)
                        .items(List.of())
                        .build();
            }

            List<PurchaseRecommendItemVO> items = new ArrayList<>();
            for (int i = 0; i < itemsArray.size(); i++) {
                JSONObject obj = itemsArray.getJSONObject(i);
                PurchaseRecommendItemVO item = parseRecommendItem(obj, validCategoryMap, validUnitMap, unitTypeMap);
                if (item != null) {
                    items.add(item);
                }
            }

            return PurchaseRecommendVO.builder()
                    .sufficientData(true)
                    .tips(tips)
                    .items(items)
                    .build();
        } catch (Exception e) {
            log.warn("AI 推荐响应解析失败，响应：{}", cleaned, e);
            return emptyResult();
        }
    }

    /**
     * 解析单个推荐物品。
     */
    private PurchaseRecommendItemVO parseRecommendItem(JSONObject obj,
                                                       Map<Long, String> validCategoryMap,
                                                       Map<Long, BizItemUnit> validUnitMap,
                                                       Map<Long, String> unitTypeMap) {
        String itemName = obj.getStr("itemName");
        if (itemName == null || itemName.isBlank()) {
            return null;
        }

        BigDecimal plannedNum = obj.getBigDecimal("plannedNum");
        if (plannedNum == null || plannedNum.compareTo(BigDecimal.ZERO) <= 0) {
            plannedNum = BigDecimal.ONE;
        }

        // 单位 ID 白名单校验
        Long unitId = obj.getLong("unitId");
        String unitName = null;
        if (unitId != null && validUnitMap.containsKey(unitId)) {
            unitName = validUnitMap.get(unitId).getUnitName();
        } else {
            unitId = null;
        }

        // 分类 ID 白名单校验
        Long categoryId = obj.getLong("categoryId");
        String categoryName = null;
        if (categoryId != null && validCategoryMap.containsKey(categoryId)) {
            categoryName = validCategoryMap.get(categoryId);
        } else {
            categoryId = null;
        }

        Boolean essential = obj.getBool("isEssential");
        Boolean storeInFridge = obj.getBool("storeInFridge", true);

        Long unitTypeId = null;
        String unitTypeName = null;
        if (unitId != null && validUnitMap.containsKey(unitId)) {
            unitTypeId = validUnitMap.get(unitId).getUnitTypeId();
            unitTypeName = unitTypeMap.get(unitTypeId);
        }

        return PurchaseRecommendItemVO.builder()
                .itemName(itemName.trim())
                .plannedNum(plannedNum)
                .unitId(unitId)
                .unitName(unitName)
                .unitTypeId(unitTypeId)
                .unitTypeName(unitTypeName)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .reason(obj.getStr("reason"))
                .essential(essential)
                .storeInFridge(storeInFridge)
                .build();
    }

    /**
     * 读取场景模板描述。
     */
    private String loadSceneDesc(SceneTemplate template) {
        String key = template.getPromptFile().replace(".md", "");
        String content = promptLoader.getPrompt(key, null);
        return content != null ? content.trim() : "";
    }

    private String formatInventoryList(List<BizFridgeItem> items, Map<Long, BizItemUnit> unitMap) {
        if (items == null || items.isEmpty()) {
            return "（暂无库存）";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            BizFridgeItem item = items.get(i);
            sb.append(i + 1).append(". ").append(item.getItemName());
            if (item.getItemNum() != null) {
                sb.append(" ").append(item.getItemNum().stripTrailingZeros().toPlainString());
                String unitName = null;
                if (item.getItemUnitId() != null && unitMap != null) {
                    BizItemUnit unit = unitMap.get(item.getItemUnitId());
                    if (unit != null) {
                        unitName = unit.getUnitName();
                    }
                }
                if (unitName != null && !unitName.isBlank()) {
                    sb.append(unitName);
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String formatAddRecords(List<BizItemAddRecord> records) {
        if (records == null || records.isEmpty()) {
            return "（近30天无入库记录）";
        }
        StringBuilder sb = new StringBuilder();
        for (BizItemAddRecord r : records) {
            sb.append("- ").append(r.getItemName()).append(" ")
                    .append(r.getAddNum() != null ? r.getAddNum().stripTrailingZeros().toPlainString() : "0");
            if (r.getUnitName() != null && !r.getUnitName().isBlank()) {
                sb.append(r.getUnitName());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String formatTakeOutRecords(List<BizItemTakeOutRecord> records) {
        if (records == null || records.isEmpty()) {
            return "（近30天无取出记录）";
        }
        StringBuilder sb = new StringBuilder();
        for (BizItemTakeOutRecord r : records) {
            sb.append("- ").append(r.getItemName()).append(" ")
                    .append(r.getTakeOutNum() != null ? r.getTakeOutNum().stripTrailingZeros().toPlainString() : "0");
            if (r.getUnitName() != null && !r.getUnitName().isBlank()) {
                sb.append(r.getUnitName());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String formatCategories(Map<Long, String> categoryMap) {
        if (categoryMap.isEmpty()) {
            return "（暂无可用分类）";
        }
        return categoryMap.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));
    }

    private String formatUnits(Map<Long, BizItemUnit> unitMap, Map<Long, String> unitTypeMap) {
        if (unitMap.isEmpty()) {
            return "（暂无可用单位）";
        }
        return unitMap.values().stream()
                .map(u -> u.getId() + ": " + u.getUnitName())
                .collect(Collectors.joining("\n"));
    }

    private String cleanJsonResponse(String response) {
        if (response == null) {
            return "";
        }
        String trimmed = response.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf("{");
            int end = trimmed.lastIndexOf("}");
            if (start >= 0 && end > start) {
                return trimmed.substring(start, end + 1);
            }
        }
        return trimmed;
    }

    private PurchaseRecommendVO emptyResult() {
        return PurchaseRecommendVO.builder()
                .sufficientData(false)
                .insufficientReason("AI 推荐解析失败，请稍后再试。")
                .tips(List.of())
                .items(List.of())
                .build();
    }

    private String getStorageLocationFallbackPrompt() {
        return """
                你是一名冰箱收纳助手。请为以下需要放入冰箱的物品推荐合适的存放位置。

                冰箱常见存放位置：冷藏室、冷藏室门架、冷藏室抽屉、冷冻室、冷冻室抽屉。

                请根据物品名称、分类和保质期推荐最合适的位置：
                - 生鲜肉类、海鲜、冷冻食品 → 冷冻室或冷冻室抽屉
                - 蔬菜水果 → 冷藏室抽屉
                - 蛋类、乳制品、饮料、熟食 → 冷藏室或冷藏室门架
                - 其他需要冷藏的物品 → 冷藏室

                待推荐物品（按序号）：
                {{items}}

                要求：
                1. 输出必须是合法 JSON，不要 Markdown 代码块，不要任何额外解释文字，字符串值使用双引号，禁止尾逗号。
                2. 返回格式：{"locations": [{"index": 0, "storageLocation": "冷冻室"}, ...]}
                3. index 必须严格对应输入物品的序号（从 0 开始）。
                4. storageLocation 只能是上述常见位置之一，尽量简短。
                5. 每个输入物品都必须返回一个位置，不能遗漏。
                """;
    }

    private String getDailyFallbackPrompt() {
        return """
                你是一名专业的的采购助手。请根据用户冰箱的实际情况，推荐本次日常采购清单。

                【当前日期】
                {{today}}

                冰箱信息：
                - 名称：{{fridgeName}}
                - 容量利用率：{{capacityRate}}%
                - 总容量：{{totalCapacity}} 升

                当前库存：
                {{inventoryList}}

                近30天入库记录：
                {{addRecords}}

                近30天取出记录：
                {{takeOutRecords}}

                高频消耗物品（取出次数Top 10）：
                {{frequentItems}}

                可选分类（id: 名称）：
                {{categories}}

                可选单位（id: 名称）：
                {{units}}

                要求：
                1. 推荐 5-10 个物品，给出建议采购数量。
                2. 优先补充库存低、消耗快的物品。
                3. 若容量利用率超过 85%，建议减少新增或优先消耗库存。
                4. 若容量利用率超过 95%，在 tips 中增加"冰箱容量紧张，建议优先消耗库存"。
                5. 格式化为 JSON，外层包含 sufficientData=true, insufficientReason=null, tips, items 数组。
                6. items 每个元素包含：itemName, plannedNum, unitId, categoryId, storeInFridge, reason
                   - unitId 和 categoryId 必须从上述可选列表中选择，返回对应ID。
                   - 没有合适分类或单位时返回 null。
                   - storeInFridge 表示采购后是否建议存入冰箱（true/false）。例如大米、调料等常温物品返回 false；肉类、蔬菜、乳制品等返回 true。
                7. tips 必须是字符串数组，提供 1-3 条采购建议；如果没有建议可返回空数组 []。
                8. 若当前库存、近30天入库/取出记录均严重不足（如库存不足3种且没有任何历史记录），可将 sufficientData 设为 false，并在 insufficientReason 中说明原因，items 设为空数组。
                9. 输出必须是一行合法JSON，不要 Markdown 代码块，不要任何额外解释文字，字符串值使用双引号，禁止尾逗号。

                示例：
                当前库存：
                1. 鸡蛋 2个
                2. 牛奶 0.5升
                3. 大米 5千克
                4. 苹果 3个
                5. 西红柿 1个

                输出：
                {
                  "sufficientData": true,
                  "insufficientReason": null,
                  "tips": ["鸡蛋库存偏低，建议优先补充", "牛奶剩余不足1升，可按家庭日消耗量采购"],
                  "items": [
                    {"itemName": "鸡蛋", "plannedNum": 20, "unitId": 1, "categoryId": 2, "storeInFridge": true, "reason": "库存仅剩2个，日常消耗快"},
                    {"itemName": "牛奶", "plannedNum": 2, "unitId": 3, "categoryId": 4, "storeInFridge": true, "reason": "库存不足1升，需补充"},
                    {"itemName": "大米", "plannedNum": 5, "unitId": 5, "categoryId": 6, "storeInFridge": false, "reason": "日常主食，常温保存即可"}
                  ]
                }
                """;
    }

    private String getSpecialFallbackPrompt() {
        return """
                你是一名专业的采购助手。请根据用户场景以及用户的冰箱情况生成采购清单。

                【当前日期】
                {{today}}

                场景：{{sceneDesc}}

                目标冰箱：{{fridgeName}}
                现有库存：
                {{inventoryList}}

                冰箱容量情况：
                - 总容量：{{totalCapacity}}
                - 容量利用率：{{capacityRate}}

                {{capacityHint}}

                参与人数/数量：{{estimatedPeople}}

                可选分类（id: 名称）：
                {{categories}}

                可选单位（id: 名称）：
                {{units}}

                要求：
                1. 生成完整的采购清单，必须包含食材、调料、辅料中场景必需的物品，即使库存中已有同类物品，只要数量预估不足，也应列入清单并说明原因。
                2. 必须至少生成 1 件物品，除非场景本身无需任何采购（例如"查看库存"类场景，此时请在 tips 中说明原因，并将 items 设为空数组）。
                3. 对库存中已有的物品，请根据参与人数/数量评估消耗量，如需补购，在 reason 中注明"库存不足，需补充X份"。
                4. 场景核心食材（如火锅的肉类/锅底、烧烤的肉类/调料）isEssential 必须为 true；锦上添花类为 false；至少应有 1 件物品的 isEssential 为 true。
                5. 参考上述"冰箱容量情况"。若容量信息完整且利用率超过 85%，建议减少新增或优先消耗库存；若超过 95%，请在 tips 中增加"冰箱容量紧张，建议优先消耗库存"。若容量信息未维护（总容量未设置或利用率未统计），则按场景需求正常推荐，无需考虑容量限制。
                6. 格式化为 JSON，外层包含 sufficientData=true, insufficientReason=null, tips, items 数组。
                7. items 每个元素包含：itemName, plannedNum, unitId, categoryId, isEssential, storeInFridge, reason
                   - unitId 和 categoryId 必须从上述可选列表中选择，返回对应ID。
                   - 如果没有完全匹配的分类或单位，请选择最接近的并返回其ID，实在无匹配时返回 null。
                   - isEssential 表示是否必需（true/false）。
                   - storeInFridge 表示采购后是否建议存入冰箱（true/false）。例如大米、黑胡椒、火锅底料等常温或调料类物品应返回 false；肉类、蔬菜、乳制品等应返回 true。
                8. tips 必须是字符串数组，提供 1-2 条采购建议；如果没有建议可返回空数组 []。
                9. 输出必须是一行合法JSON，不要 Markdown 代码块，不要任何额外解释文字，字符串值使用双引号，禁止尾逗号。

                示例：
                场景：周末家庭火锅聚餐，预计4人参与
                现有库存：
                1. 牛肉卷 0盒
                2. 羊肉卷 1盒
                3. 土豆 3个
                4. 金针菇 1包

                输出：
                {
                  "sufficientData": true,
                  "insufficientReason": null,
                  "tips": ["火锅底料和蘸料通常不放在冰箱里，请按需准备", "蔬菜类建议当天购买"],
                  "items": [
                    {"itemName": "牛肉卷", "plannedNum": 2, "unitId": 5, "categoryId": 6, "isEssential": true, "storeInFridge": true, "reason": "库存为0，火锅主材必需"},
                    {"itemName": "火锅底料", "plannedNum": 1, "unitId": 9, "categoryId": 10, "isEssential": true, "storeInFridge": false, "reason": "火锅必需调料，通常常温保存"},
                    {"itemName": "金针菇", "plannedNum": 2, "unitId": 7, "categoryId": 8, "isEssential": false, "storeInFridge": true, "reason": "库存只有1包，4人聚餐建议增量"}
                  ]
                }
                """;
    }

    /**
     * 数据聚合内部类。
     */
    @Setter
    @Getter
    private static class DataSummary {
        // Getters and Setters
        private List<BizFridgeItem> items;
        private List<BizItemAddRecord> addRecords;
        private List<BizItemTakeOutRecord> takeOutRecords;
        private List<String> frequentItems;
        private int capacityRate;
        private int itemCount;
        private Integer totalCapacity;
        private boolean capacityStatsAvailable;
        private Map<Long, String> validCategoryMap;
        private Map<Long, BizItemUnit> validUnitMap;
        private Map<Long, String> unitTypeMap;

        public boolean isSufficient() {
            return items.size() >= DATA_SUFFICIENCY_THRESHOLD
                    && (addRecords != null ? addRecords.size() : 0) >= DATA_SUFFICIENCY_THRESHOLD
                    && (takeOutRecords != null ? takeOutRecords.size() : 0) >= DATA_SUFFICIENCY_THRESHOLD;
        }

    }
}
