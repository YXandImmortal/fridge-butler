package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.entity.BizFridge;
import com.yx.fridgebutler.entity.BizFridgeCapacityRate;
import com.yx.fridgebutler.entity.BizFridgeItem;
import com.yx.fridgebutler.entity.BizItemCategory;
import com.yx.fridgebutler.entity.BizItemUnit;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.BizFridgeCapacityRateRepository;
import com.yx.fridgebutler.repository.BizFridgeItemRepository;
import com.yx.fridgebutler.repository.BizFridgeRepository;
import com.yx.fridgebutler.repository.BizFridgeTypeRepository;
import com.yx.fridgebutler.repository.BizItemAddRecordRepository;
import com.yx.fridgebutler.repository.BizItemCategoryRepository;
import com.yx.fridgebutler.repository.BizItemChangeRecordRepository;
import com.yx.fridgebutler.repository.BizItemTakeOutRecordRepository;
import com.yx.fridgebutler.repository.BizItemUnitRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.CapacityStatsService;
import com.yx.fridgebutler.service.DeepSeekService;
import com.yx.fridgebutler.service.NotificationService;
import com.yx.fridgebutler.vo.CapacityStatsVO;
import com.yx.fridgebutler.vo.FridgeCapacityRateVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 容量利用率统计服务实现类。
 * <p>基于 DeepSeek AI 大模型估算冰箱空间占用率，支持缓存与异步更新机制。</p>
 */
@Slf4j
@Service
public class CapacityStatsServiceImpl implements CapacityStatsService {

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private BizFridgeItemRepository itemRepository;

    @Autowired
    private BizFridgeCapacityRateRepository capacityRateRepository;

    @Autowired
    private BizItemCategoryRepository categoryRepository;

    @Autowired
    private BizItemUnitRepository unitRepository;

    @Autowired
    private BizItemAddRecordRepository addRecordRepository;

    @Autowired
    private BizItemTakeOutRecordRepository takeOutRecordRepository;

    @Autowired
    private BizItemChangeRecordRepository changeRecordRepository;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private BizFridgeTypeRepository fridgeTypeRepository;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    @Qualifier("capacityStatsExecutor")
    private Executor executor;

    private static final long REFRESH_INTERVAL_HOURS = 1;
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");

    /**
     * {@inheritDoc}
     * <p>
     * 查询逻辑：
     * <ol>
     *   <li>查询当前用户所有冰箱</li>
     *   <li>查询各冰箱的缓存记录</li>
     *   <li>无缓存的冰箱触发同步计算（确保首次使用有数据）</li>
     *   <li>缓存过期且用户未被限流的冰箱触发异步更新</li>
     *   <li>组装并返回统计结果</li>
     * </ol>
     */
    @Override
    public CapacityStatsVO getCapacityStats(Long fridgeId) {
        Long currentUserId = getCurrentUserId();
        log.info("查询容量利用率统计，用户ID：{}，冰箱ID：{}", currentUserId, fridgeId);

        List<BizFridge> fridges;
        if (fridgeId != null) {
            BizFridge fridge = fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(fridgeId, currentUserId)
                    .orElseThrow(BusinessException::fridgeNotFound);
            fridges = Collections.singletonList(fridge);
        } else {
            Sort sort = Sort.by(Sort.Direction.ASC, "createTime");
            fridges = fridgeRepository.findByOwnerIdAndIsDeletedFalse(currentUserId, sort);
        }

        if (fridges.isEmpty()) {
            return CapacityStatsVO.builder()
                    .avgRate(0)
                    .fridgeRates(Collections.emptyList())
                    .build();
        }

        List<Long> fridgeIds = fridges.stream()
                .map(BizFridge::getId)
                .toList();

        List<BizFridgeCapacityRate> caches = capacityRateRepository.findByFridgeIdIn(fridgeIds);
        Map<Long, BizFridgeCapacityRate> cacheMap = caches.stream()
                .collect(Collectors.toMap(BizFridgeCapacityRate::getFridgeId, c -> c));

        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(REFRESH_INTERVAL_HOURS, ChronoUnit.HOURS);

        boolean hasRecentCalculation = caches.stream()
                .anyMatch(c -> c.getLastCalculateTime().isAfter(oneHourAgo));

        List<BizFridge> firstTimeFridges = new ArrayList<>();
        List<BizFridge> expiredFridges = new ArrayList<>();

        for (BizFridge fridge : fridges) {
            BizFridgeCapacityRate cache = cacheMap.get(fridge.getId());
            if (cache == null) {
                firstTimeFridges.add(fridge);
            } else if (cache.getLastCalculateTime().isBefore(oneHourAgo) && !hasRecentCalculation) {
                if (needRecalculate(fridge, cache)) {
                    expiredFridges.add(fridge);
                } else {
                    extendCacheTime(cache);
                    log.info("冰箱数据无变化，延长缓存有效期，冰箱ID：{}", fridge.getId());
                }
            }
        }

        // 首次使用：同步计算，否则用户永远看不到数据
        for (BizFridge fridge : firstTimeFridges) {
            try {
                calculateAndSave(fridge);
            } catch (Exception e) {
                log.error("同步计算冰箱容量利用率失败，冰箱ID：{}", fridge.getId(), e);
            }
        }

        // 缓存过期且用户未被限流：异步更新
        if (!expiredFridges.isEmpty()) {
            for (BizFridge fridge : expiredFridges) {
                executor.execute(() -> {
                    try {
                        log.info("异步计算冰箱容量利用率，冰箱ID：{}", fridge.getId());
                        calculateAndSave(fridge);
                    } catch (Exception e) {
                        log.error("异步计算冰箱容量利用率失败，冰箱ID：{}", fridge.getId(), e);
                    }
                });
            }
        }

        // 重新查询缓存（包含刚同步计算的结果）
        caches = capacityRateRepository.findByFridgeIdIn(fridgeIds);
        cacheMap = caches.stream()
                .collect(Collectors.toMap(BizFridgeCapacityRate::getFridgeId, c -> c));

        // 组装返回数据
        List<FridgeCapacityRateVO> fridgeRates = new ArrayList<>();
        int totalRate = 0;

        for (BizFridge fridge : fridges) {
            BizFridgeCapacityRate cache = cacheMap.get(fridge.getId());
            int rate = (cache != null) ? cache.getRate() : 0;
            int itemCount = (cache != null) ? cache.getItemCount() : 0;

            fridgeRates.add(FridgeCapacityRateVO.builder()
                    .fridgeId(fridge.getId())
                    .fridgeName(fridge.getFridgeName())
                    .rate(rate)
                    .itemCount(itemCount)
                    .totalCapacity(fridge.getTotalCapacity())
                    .build());

            totalRate += rate;
        }

        int avgRate = Math.round((float) totalRate / fridges.size());

        return CapacityStatsVO.builder()
                .avgRate(avgRate)
                .fridgeRates(fridgeRates)
                .build();
    }

    /**
     * 计算单个冰箱的容量利用率并保存缓存。
     *
     * @param fridge 冰箱实体
     */
    private void calculateAndSave(BizFridge fridge) {
        Long fridgeId = fridge.getId();

        Optional<BizFridgeCapacityRate> optionalCache = capacityRateRepository.findByFridgeId(fridgeId);
        if (optionalCache.isPresent()) {
            BizFridgeCapacityRate cache = optionalCache.get();
            if (!needRecalculate(fridge, cache)) {
                log.info("冰箱数据无变化，跳过AI计算，冰箱ID：{}", fridgeId);
                extendCacheTime(cache);
                return;
            }
        }

        List<BizFridgeItem> items = itemRepository.findByFridgeIdAndIsDeletedFalse(fridgeId);

        if (fridge.getTotalCapacity() == null || fridge.getTotalCapacity() <= 0) {
            log.warn("冰箱总容量未设置，无法计算占用率，冰箱ID：{}", fridgeId);
            saveCache(fridge, 0, items.size());
            return;
        }

        // 加载分类和单位映射
        Map<Long, String> categoryMap = loadCategoryMap(items);
        Map<Long, String> unitMap = loadUnitMap(items);

        String prompt = buildPrompt(fridge, items, categoryMap, unitMap);

        String systemMessage = "你是一位家庭收纳专家，擅长根据冰箱内的物品种类、数量和规格，估算它们占用的空间比例。请根据生活常识进行合理估算，考虑物品的实际形状、包装方式以及冰箱内部空间的实际利用情况（并非100%填满）。只返回一个0-100之间的整数，表示容量占用百分比，不要有任何额外解释。";

        try {
            String response = deepSeekService.chat(systemMessage, prompt);
            int rate = parseRate(response);
            saveCache(fridge, rate, items.size());
            log.info("冰箱容量利用率计算完成，冰箱ID：{}，占用率：{}%", fridgeId, rate);
        } catch (Exception e) {
            log.error("AI 计算冰箱容量利用率失败，冰箱ID：{}", fridgeId, e);
            // 保存失败时的降级数据：如果有旧缓存则保留，否则存0
            Optional<BizFridgeCapacityRate> oldCache = capacityRateRepository.findByFridgeId(fridgeId);
            if (oldCache.isEmpty()) {
                saveCache(fridge, 0, items.size());
            }
        }
    }

    /**
     * 加载物品分类名称映射。
     *
     * @param items 物品列表
     * @return 分类ID到分类名称的映射
     */
    private Map<Long, String> loadCategoryMap(List<BizFridgeItem> items) {
        Set<Long> categoryIds = items.stream()
                .map(BizFridgeItem::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(BizItemCategory::getId, BizItemCategory::getCategoryName));
    }

    /**
     * 加载物品单位名称映射。
     *
     * @param items 物品列表
     * @return 单位ID到单位名称的映射
     */
    private Map<Long, String> loadUnitMap(List<BizFridgeItem> items) {
        Set<Long> unitIds = items.stream()
                .map(BizFridgeItem::getItemUnitId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (unitIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return unitRepository.findAllById(unitIds).stream()
                .collect(Collectors.toMap(BizItemUnit::getId, BizItemUnit::getUnitName));
    }

    /**
     * 构造发给 AI 的 Prompt。
     *
     * @param fridge      冰箱实体
     * @param items       物品列表
     * @param categoryMap 分类映射
     * @param unitMap     单位映射
     * @return Prompt 文本
     */
    private String buildPrompt(BizFridge fridge, List<BizFridgeItem> items,
                               Map<Long, String> categoryMap, Map<Long, String> unitMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("请估算以下冰箱的空间占用率。\n\n");

        // 追加冰箱类型信息
        if (fridge.getFridgeTypeId() != null) {
            fridgeTypeRepository.findByIdAndIsDeletedFalse(fridge.getFridgeTypeId())
                    .ifPresent(type -> sb.append("冰箱类型：").append(type.getTypeName()).append("\n"));
        }

        sb.append("冰箱总容积：").append(fridge.getTotalCapacity()).append(" 升\n");
        sb.append("冰箱名称：").append(fridge.getFridgeName()).append("\n");

        // 追加备注信息（仅供参考）
        if (fridge.getRemark() != null && !fridge.getRemark().isBlank()) {
            sb.append("冰箱备注（仅供参考）：").append(fridge.getRemark()).append("\n");
        }

        sb.append("\n冰箱内物品清单（共 ").append(items.size()).append(" 种）：\n");

        for (int i = 0; i < items.size(); i++) {
            BizFridgeItem item = items.get(i);
            sb.append(i + 1).append(". ");
            sb.append("物品名称：").append(item.getItemName()).append("，");

            String category = categoryMap.get(item.getCategoryId());
            if (category != null) {
                sb.append("分类：").append(category).append("，");
            }

            sb.append("数量：").append(formatItemNum(item.getItemNum()));

            String unit = unitMap.get(item.getItemUnitId());
            if (unit != null) {
                sb.append(" ").append(unit);
            }
            sb.append("\n");
        }

        sb.append("\n请只返回一个 0-100 的整数，表示该冰箱的容量占用率百分比。不要返回任何解释或额外文字。");
        return sb.toString();
    }

    /**
     * 格式化物品数量，去除末尾无意义的零。
     *
     * @param num 物品数量
     * @return 格式化后的字符串
     */
    private String formatItemNum(BigDecimal num) {
        if (num == null) {
            return "0";
        }
        return num.stripTrailingZeros().toPlainString();
    }

    /**
     * 从 AI 响应中解析出占用率百分比。
     *
     * @param response AI 原始响应文本
     * @return 0-100 的整数，解析失败返回 0
     */
    private int parseRate(String response) {
        if (response == null || response.isBlank()) {
            return 0;
        }
        Matcher matcher = DIGIT_PATTERN.matcher(response);
        if (matcher.find()) {
            try {
                int rate = Integer.parseInt(matcher.group());
                return Math.clamp(rate, 0, 100);
            } catch (NumberFormatException e) {
                log.warn("无法解析AI返回的占用率数字：{}", response);
            }
        }
        log.warn("AI返回中未找到数字，原始内容：{}", response);
        return 0;
    }

    /**
     * 保存或更新容量利用率缓存。
     *
     * @param fridge    冰箱实体
     * @param rate      占用率百分比
     * @param itemCount 物品数量
     */
    private void saveCache(BizFridge fridge, int rate, int itemCount) {
        Optional<BizFridgeCapacityRate> optional = capacityRateRepository.findByFridgeId(fridge.getId());
        BizFridgeCapacityRate cache = optional.orElseGet(() -> {
            BizFridgeCapacityRate c = new BizFridgeCapacityRate();
            c.setFridgeId(fridge.getId());
            c.setCreateTime(Instant.now());
            c.setIsDeleted((byte) 0);
            return c;
        });

        cache.setRate(rate);
        cache.setItemCount(itemCount);
        cache.setTotalCapacity(fridge.getTotalCapacity());
        cache.setFridgeTypeId(fridge.getFridgeTypeId());
        cache.setLastCalculateTime(Instant.now());
        cache.setUpdateTime(Instant.now());

        capacityRateRepository.save(cache);

        // 容量预警判断：利用率超过 80% 时生成预警通知，恢复至 80% 及以下时清除预警
        if (rate > 80) {
            notificationService.createCapacityWarningIfAbsent(fridge, rate);
        } else {
            notificationService.clearCapacityWarning(fridge.getId());
        }
    }

    /**
     * 判断是否需要重新计算冰箱容量利用率。
     * <p>基于「总容量比对 + 冰箱类型比对 + 记录表兜底」策略：</p>
     * <ol>
     *   <li>冰箱总容量发生变化</li>
     *   <li>冰箱类型发生变化</li>
     *   <li>上次计算后有新增物品记录</li>
     *   <li>上次计算后有取出物品记录</li>
     *   <li>上次计算后有数量变更记录（UPDATE_NUM）</li>
     * </ol>
     *
     * @param fridge 冰箱实体
     * @param cache  缓存记录
     * @return true 表示需要重新计算
     */
    private boolean needRecalculate(BizFridge fridge, BizFridgeCapacityRate cache) {
        // 1. 冰箱总容量发生变化
        if (!Objects.equals(cache.getTotalCapacity(), fridge.getTotalCapacity())) {
            log.info("冰箱总容量变化，需要重新计算，冰箱ID：{}，旧容量：{}，新容量：{}",
                    fridge.getId(), cache.getTotalCapacity(), fridge.getTotalCapacity());
            return true;
        }

        // 2. 冰箱类型发生变化（不同类型冷藏/冷冻区域布局不同，直接影响空间利用率判断）
        if (!Objects.equals(cache.getFridgeTypeId(), fridge.getFridgeTypeId())) {
            log.info("冰箱类型变化，需要重新计算，冰箱ID：{}，旧类型ID：{}，新类型ID：{}",
                    fridge.getId(), cache.getFridgeTypeId(), fridge.getFridgeTypeId());
            return true;
        }

        Instant lastTime = cache.getLastCalculateTime();

        // 3. 上次计算后有新增物品记录
        if (addRecordRepository.existsByFridgeIdAndCreateTimeAfter(fridge.getId(), lastTime)) {
            log.info("冰箱有新增物品记录，需要重新计算，冰箱ID：{}", fridge.getId());
            return true;
        }

        // 4. 上次计算后有取出物品记录
        if (takeOutRecordRepository.existsByFridgeIdAndCreateTimeAfter(fridge.getId(), lastTime)) {
            log.info("冰箱有取出物品记录，需要重新计算，冰箱ID：{}", fridge.getId());
            return true;
        }

        // 5. 上次计算后有数量变更记录
        if (changeRecordRepository.existsByFridgeIdAndCreateTimeAfterAndChangeType(
                fridge.getId(), lastTime, "UPDATE_NUM")) {
            log.info("冰箱有数量变更记录，需要重新计算，冰箱ID：{}", fridge.getId());
            return true;
        }

        return false;
    }

    /**
     * 延长缓存有效期，仅更新最后计算时间。
     *
     * @param cache 缓存记录
     */
    private void extendCacheTime(BizFridgeCapacityRate cache) {
        cache.setLastCalculateTime(Instant.now());
        cache.setUpdateTime(Instant.now());
        capacityRateRepository.save(cache);
    }

    /**
     * 获取当前登录用户的ID。
     *
     * @return 当前用户ID
     * @throws BusinessException 如果当前用户不存在则抛出异常
     */
    private Long getCurrentUserId() {
        String username = getUsernameFromToken();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);
        return user.getId();
    }

    /**
     * 从 Spring Security 上下文中获取当前登录用户名。
     *
     * @return 当前用户名
     * @throws BusinessException 如果未获取到认证信息则抛出异常
     */
    private static String getUsernameFromToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw BusinessException.authFailed();
        }
        return authentication.getName();
    }
}
