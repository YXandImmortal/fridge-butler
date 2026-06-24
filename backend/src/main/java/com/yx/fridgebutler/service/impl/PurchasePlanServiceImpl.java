package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.purchase.PurchasePlanCreateRequest;
import com.yx.fridgebutler.dto.purchase.PurchasePlanEmailContext;
import com.yx.fridgebutler.dto.purchase.PurchasePlanItemCreateRequest;
import com.yx.fridgebutler.dto.purchase.PurchasePlanSettleItemRequest;
import com.yx.fridgebutler.dto.purchase.PurchasePlanSettleRequest;
import com.yx.fridgebutler.dto.purchase.PurchasePlanUpdateRequest;
import com.yx.fridgebutler.entity.*;
import com.yx.fridgebutler.enums.BadgeTriggerType;
import com.yx.fridgebutler.enums.ExpActionType;
import com.yx.fridgebutler.enums.PurchasePlanItemStatus;
import com.yx.fridgebutler.enums.PurchasePlanSource;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.*;
import com.yx.fridgebutler.dto.purchase.StorageLocationSuggestRequest;
import com.yx.fridgebutler.service.AchievementSettlementService;
import com.yx.fridgebutler.service.CapacityStatsService;
import com.yx.fridgebutler.service.EmailService;
import com.yx.fridgebutler.service.PurchaseIntelligenceService;
import com.yx.fridgebutler.service.PurchasePlanService;
import com.yx.fridgebutler.util.EmailUtil;
import com.yx.fridgebutler.util.UserContextUtil;
import com.yx.fridgebutler.vo.gamification.AchievementSettlementResult;
import com.yx.fridgebutler.vo.gamification.BadgeTriggerRequest;
import com.yx.fridgebutler.vo.gamification.ExpActionRequest;
import com.yx.fridgebutler.vo.purchase.PurchasePlanItemVO;
import com.yx.fridgebutler.vo.purchase.PurchasePlanSettleResultVO;
import com.yx.fridgebutler.vo.purchase.PurchasePlanVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采购方案服务实现。
 */
@Slf4j
@Service
public class PurchasePlanServiceImpl implements PurchasePlanService {

    /** 待采购状态。 */
    private static final Byte STATUS_PENDING = 1;
    /** 已完成状态。 */
    private static final Byte STATUS_COMPLETED = 2;
    /** 已取消状态。 */
    private static final Byte STATUS_CANCELLED = 3;



    @Autowired
    private BizPurchasePlanRepository planRepository;

    @Autowired
    private BizPurchasePlanItemRepository planItemRepository;

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private BizFridgeItemRepository itemRepository;

    @Autowired
    private BizItemAddRecordRepository addRecordRepository;

    @Autowired
    private BizItemCategoryRepository categoryRepository;

    @Autowired
    private BizItemUnitRepository unitRepository;

    @Autowired
    private BizUnitTypeRepository unitTypeRepository;

    @Autowired
    private CapacityStatsService capacityStatsService;

    @Autowired
    private PurchaseIntelligenceService purchaseIntelligenceService;

    @Autowired
    private AchievementSettlementService achievementSettlementService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SpringTemplateEngine templateEngine;

    /** 上海时区，用于邮件创建时间格式化。 */
    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");

    /** 邮件创建时间格式化器，格式为 yyyy年M月d日。 */
    private static final DateTimeFormatter EMAIL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年M月d日");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchasePlanVO createPlan(PurchasePlanCreateRequest request, PurchasePlanSource source) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        log.info("创建采购方案，用户ID：{}，冰箱ID：{}，来源：{}", currentUserId, request.getFridgeId(), source);

        // 校验冰箱归属
        BizFridge fridge = fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(request.getFridgeId(), currentUserId)
                .orElseThrow(BusinessException::fridgeNotFound);

        // 校验物品清单中的分类和单位
        validateItems(request.getItems());

        // 保存方案
        BizPurchasePlan plan = new BizPurchasePlan();
        plan.setUserId(currentUserId);
        plan.setFridgeId(fridge.getId());
        plan.setPlanName(request.getPlanName());
        plan.setSource(source.getCode());
        plan.setPlanStatus(STATUS_PENDING);
        plan.setSceneDesc(request.getSceneDesc());
        plan.setTotalItems(request.getItems().size());
        plan.setCompletedItems(0);

        Instant now = Instant.now();
        plan.setCreateTime(now);
        plan.setUpdateTime(now);

        BizPurchasePlan savedPlan = planRepository.save(plan);

        // 保存方案物品
        List<BizPurchasePlanItem> items = createPlanItems(savedPlan.getId(), request.getItems());
        planItemRepository.saveAll(items);

        // 游戏化结算
        AchievementSettlementResult settlement = achievementSettlementService.settle(
                currentUserId,
                ExpActionType.CREATE_PURCHASE_PLAN,
                BadgeTriggerType.CREATE_PURCHASE_PLAN,
                Map.of("source", source.getCode()));

        PurchasePlanVO vo = convertToVO(savedPlan, items, fridge.getFridgeName());
        vo.setExpGained(settlement.getExpGained());
        vo.setBadgeExpTotal(settlement.getBadgeExpTotal());
        vo.setDailyExpToday(settlement.getDailyExpToday());
        vo.setDailyExpLimit(settlement.getDailyExpLimit());
        vo.setLeveledUp(settlement.isLeveledUp());
        vo.setCurrentLevel(settlement.getCurrentLevel());
        vo.setLevel(settlement.getLevel());
        vo.setBadgesUnlocked(settlement.getBadgesUnlocked());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchasePlanVO updatePlan(Long id, PurchasePlanUpdateRequest request) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        log.info("修改采购方案，用户ID：{}，方案ID：{}", currentUserId, id);

        BizPurchasePlan plan = planRepository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(BusinessException::purchasePlanNotFound);

        if (!STATUS_PENDING.equals(plan.getPlanStatus())) {
            throw BusinessException.purchasePlanCannotUpdate();
        }

        // 校验物品清单
        validateItems(request.getItems());

        // 删除旧物品
        List<BizPurchasePlanItem> oldItems = planItemRepository.findByPlanId(id);
        planItemRepository.deleteAll(oldItems);

        // 保存新物品
        List<BizPurchasePlanItem> newItems = createPlanItems(id, request.getItems());
        planItemRepository.saveAll(newItems);

        // 更新方案
        plan.setPlanName(request.getPlanName());
        plan.setSceneDesc(request.getSceneDesc());
        plan.setTotalItems(request.getItems().size());
        plan.setCompletedItems(0);
        plan.setUpdateTime(Instant.now());
        planRepository.save(plan);

        BizFridge fridge = fridgeRepository.findById(plan.getFridgeId()).orElse(null);
        return convertToVO(plan, newItems, fridge != null ? fridge.getFridgeName() : null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long id) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        log.info("删除采购方案，用户ID：{}，方案ID：{}", currentUserId, id);

        BizPurchasePlan plan = planRepository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(BusinessException::purchasePlanNotFound);

        if (!STATUS_PENDING.equals(plan.getPlanStatus()) && !STATUS_CANCELLED.equals(plan.getPlanStatus())) {
            throw BusinessException.purchasePlanCannotDelete();
        }

        List<BizPurchasePlanItem> items = planItemRepository.findByPlanId(id);
        planItemRepository.deleteAll(items);
        planRepository.delete(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPlan(Long id) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        log.info("取消采购方案，用户ID：{}，方案ID：{}", currentUserId, id);

        BizPurchasePlan plan = planRepository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(BusinessException::purchasePlanNotFound);

        if (!STATUS_PENDING.equals(plan.getPlanStatus())) {
            throw BusinessException.purchasePlanCannotCancel();
        }

        plan.setPlanStatus(STATUS_CANCELLED);
        plan.setUpdateTime(Instant.now());
        planRepository.save(plan);
    }

    @Override
    public PurchasePlanVO getPlan(Long id) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        BizPurchasePlan plan = planRepository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(BusinessException::purchasePlanNotFound);

        List<BizPurchasePlanItem> items = planItemRepository.findByPlanId(id);
        BizFridge fridge = fridgeRepository.findById(plan.getFridgeId()).orElse(null);
        return convertToVO(plan, items, fridge != null ? fridge.getFridgeName() : null);
    }

    @Override
    public List<PurchasePlanVO> listPlans(Byte planStatus) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");

        List<BizPurchasePlan> plans;
        if (planStatus != null) {
            plans = planRepository.findByUserIdAndPlanStatus(currentUserId, planStatus, sort);
        } else {
            plans = planRepository.findByUserId(currentUserId, sort);
        }

        return plans.stream()
                .map(plan -> {
                    List<BizPurchasePlanItem> items = planItemRepository.findByPlanId(plan.getId());
                    BizFridge fridge = fridgeRepository.findById(plan.getFridgeId()).orElse(null);
                    return convertToVO(plan, items, fridge != null ? fridge.getFridgeName() : null);
                })
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchasePlanSettleResultVO settle(Long planId, PurchasePlanSettleRequest request) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        log.info("采购方案入库结算，用户ID：{}，方案ID：{}", currentUserId, planId);

        BizPurchasePlan plan = planRepository.findByIdAndUserId(planId, currentUserId)
                .orElseThrow(BusinessException::purchasePlanNotFound);

        if (!STATUS_PENDING.equals(plan.getPlanStatus())) {
            throw BusinessException.purchasePlanCannotSettle();
        }

        List<PurchasePlanSettleItemRequest> settleItems = request.getItems();
        if (settleItems == null || settleItems.isEmpty()) {
            throw BusinessException.purchasePlanEmptyItems();
        }

        // 获取所有原有方案物品
        List<BizPurchasePlanItem> existingItems = planItemRepository.findByPlanId(planId);
        Map<Long, BizPurchasePlanItem> existingItemMap = existingItems.stream()
                .collect(Collectors.toMap(BizPurchasePlanItem::getId, item -> item));

        // 校验全覆盖：所有原有物品必须出现在请求中
        Set<Long> existingItemIds = existingItems.stream()
                .map(BizPurchasePlanItem::getId)
                .collect(Collectors.toSet());
        Set<Long> requestedExistingIds = settleItems.stream()
                .map(PurchasePlanSettleItemRequest::getPlanItemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!requestedExistingIds.containsAll(existingItemIds)) {
            throw BusinessException.purchasePlanItemNotCovered();
        }

        // 加载用户可用分类和单位
        List<BizItemCategory> categories = categoryRepository.findAllByOwnerIdOrSystemDefault(currentUserId);
        List<BizItemUnit> units = unitRepository.findAllByOwnerIdOrSystemDefault(currentUserId);
        Set<Long> validCategoryIds = categories.stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .map(BizItemCategory::getId)
                .collect(Collectors.toSet());
        Set<Long> validUnitIds = units.stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .map(BizItemUnit::getId)
                .collect(Collectors.toSet());

        // 单条校验
        for (PurchasePlanSettleItemRequest settleItem : settleItems) {
            validateSettleItem(settleItem, existingItemMap, validCategoryIds, validUnitIds);
        }

        // AI 补充缺失的存放位置
        fillMissingStorageLocations(settleItems, existingItemMap);

        Instant now = Instant.now();
        List<BizPurchasePlanItem> allProcessedItems = new ArrayList<>();
        int settledCount = 0;
        int notStoredCount = 0;
        int skippedCount = 0;
        BigDecimal totalDeviation = BigDecimal.ZERO;
        int deviationItemCount = 0;

        // 处理所有物品
        for (PurchasePlanSettleItemRequest settleItem : settleItems) {
            BizPurchasePlanItem planItem;
            if (settleItem.getPlanItemId() != null) {
                planItem = existingItemMap.get(settleItem.getPlanItemId());
                updateExistingItemFromSettle(planItem, settleItem);
            } else {
                planItem = createNewPlanItemFromSettle(planId, settleItem, now);
            }

            if (Boolean.TRUE.equals(settleItem.getSkip())) {
                planItem.setStatus(PurchasePlanItemStatus.SKIPPED.getCode());
                skippedCount++;
            } else {
                boolean shouldStore = settleItem.getForceStoreInFridge() != null
                        ? settleItem.getForceStoreInFridge()
                        : (settleItem.getPlanItemId() != null ? planItem.getStoreInFridge() : true);

                if (shouldStore) {
                    // 创建库存物品
                    BizFridgeItem fridgeItem = createFridgeItem(plan, planItem, settleItem, currentUserId);
                    BizFridgeItem savedItem = itemRepository.save(fridgeItem);

                    // 创建添加记录
                    String unitName = resolveUnitName(savedItem.getItemUnitId());
                    BizItemAddRecord addRecord = new BizItemAddRecord();
                    addRecord.setItemId(savedItem.getId());
                    addRecord.setFridgeId(savedItem.getFridgeId());
                    addRecord.setItemName(savedItem.getItemName());
                    addRecord.setAddNum(savedItem.getItemNum());
                    addRecord.setRemainingNum(savedItem.getItemNum());
                    addRecord.setOperatorId(currentUserId);
                    addRecord.setCreateTime(Instant.now());
                    addRecord.setItemUnitId(savedItem.getItemUnitId());
                    addRecord.setUnitName(unitName);
                    addRecordRepository.save(addRecord);

                    planItem.setStatus(PurchasePlanItemStatus.STORED.getCode());
                    settledCount++;
                } else {
                    planItem.setStatus(PurchasePlanItemStatus.PURCHASED_NOT_STORED.getCode());
                    notStoredCount++;
                }

                // 计算偏差
                if (planItem.getPlannedNum() != null && planItem.getPlannedNum().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal diff = settleItem.getActualNum().subtract(planItem.getPlannedNum()).abs();
                    BigDecimal deviation = diff.divide(planItem.getPlannedNum(), 4, RoundingMode.HALF_UP);
                    totalDeviation = totalDeviation.add(deviation);
                    deviationItemCount++;
                }
            }

            planItem.setUpdateTime(now);
            allProcessedItems.add(planItem);
        }

        planItemRepository.saveAll(allProcessedItems);

        // 更新方案为已完成
        plan.setTotalItems(allProcessedItems.size());
        plan.setCompletedItems(settledCount + notStoredCount + skippedCount);
        plan.setPlanStatus(STATUS_COMPLETED);
        plan.setUpdateTime(now);
        planRepository.save(plan);

        // 触发容量利用率重算
        try {
            capacityStatsService.getCapacityStats(plan.getFridgeId());
        } catch (Exception e) {
            log.warn("容量利用率重算失败，冰箱ID：{}", plan.getFridgeId(), e);
        }

        // 计算平均偏差
        BigDecimal avgDeviation = deviationItemCount > 0
                ? totalDeviation.divide(new BigDecimal(deviationItemCount), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 游戏化结算
        List<ExpActionRequest> actions = new ArrayList<>();
        actions.add(new ExpActionRequest(ExpActionType.BATCH_ADD_ITEM));
        actions.add(new ExpActionRequest(ExpActionType.COMPLETE_PURCHASE_PLAN));

        List<BadgeTriggerRequest> triggers = new ArrayList<>();
        triggers.add(new BadgeTriggerRequest(BadgeTriggerType.BATCH_ADD_ITEM, Map.of("count", settledCount)));
        triggers.add(new BadgeTriggerRequest(BadgeTriggerType.COMPLETE_PURCHASE_PLAN,
                Map.of("source", plan.getSource(), "deviation", avgDeviation)));

        AchievementSettlementResult settlement = achievementSettlementService.settle(
                currentUserId, actions, triggers);

        return PurchasePlanSettleResultVO.builder()
                .planId(planId)
                .settledCount(settledCount)
                .notStoredCount(notStoredCount)
                .skippedCount(skippedCount)
                .expGained(settlement.getExpGained())
                .badgeExpTotal(settlement.getBadgeExpTotal())
                .dailyExpToday(settlement.getDailyExpToday())
                .dailyExpLimit(settlement.getDailyExpLimit())
                .leveledUp(settlement.isLeveledUp())
                .currentLevel(settlement.getCurrentLevel())
                .level(settlement.getLevel())
                .badgesUnlocked(settlement.getBadgesUnlocked())
                .build();
    }

    @Override
    public void sendPlanEmail(Long id) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        log.info("发送采购方案邮件，用户ID：{}，方案ID：{}", currentUserId, id);

        // 1. 校验用户邮箱
        SysUser user = UserContextUtil.getCurrentUser();
        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            log.warn("发送采购方案邮件失败，用户未绑定邮箱，用户ID：{}，方案ID：{}", currentUserId, id);
            throw BusinessException.emailNotBound();
        }
        email = email.trim().toLowerCase();
        if (!EmailUtil.isValidEmail(email)) {
            log.warn("发送采购方案邮件失败，邮箱格式不正确，用户ID：{}，邮箱：{}", currentUserId, email);
            throw BusinessException.emailFormatError();
        }

        // 2. 校验邮件服务是否启用
        if (!emailService.isEmailEnabled()) {
            log.error("发送采购方案邮件失败，邮件服务未启用，用户ID：{}，方案ID：{}", currentUserId, id);
            throw BusinessException.emailServiceUnavailable();
        }

        // 3. 获取方案详情并校验状态
        PurchasePlanVO planVO = getPlan(id);
        if (!STATUS_PENDING.equals(planVO.getPlanStatus())) {
            log.warn("发送采购方案邮件失败，方案非待采购状态，用户ID：{}，方案ID：{}，状态：{}",
                    currentUserId, id, planVO.getPlanStatus());
            throw BusinessException.purchasePlanCannotSendEmail();
        }

        // 4. 组装 Thymeleaf 上下文
        Context context = new Context();
        context.setVariable("planName", planVO.getPlanName());
        context.setVariable("createTime", formatEmailCreateTime(planVO.getCreateTime()));
        context.setVariable("fridgeName", planVO.getFridgeName() != null ? planVO.getFridgeName() : "未选择");
        context.setVariable("sceneDesc", planVO.getSceneDesc());
        context.setVariable("items", planVO.getItems().stream()
                .map(this::convertToEmailItem)
                .toList());

        // 5. 渲染 HTML 邮件正文
        String htmlContent = templateEngine.process("mail/purchase-plan", context);

        // 6. 异步发送邮件
        String subject = "【智鲜·引擎】采购方案：" + planVO.getPlanName();
        emailService.sendHtmlMail(email, subject, htmlContent);

        log.info("采购方案邮件已提交发送，用户ID：{}，方案ID：{}，收件人：{}", currentUserId, id, email);
    }

    /**
     * 将方案物品 VO 转换为邮件模板物品项。
     */
    private PurchasePlanEmailContext.Item convertToEmailItem(PurchasePlanItemVO item) {
        return PurchasePlanEmailContext.Item.builder()
                .itemName(item.getItemName())
                .plannedNum(formatDecimal(item.getPlannedNum()))
                .itemUnitName(item.getItemUnitName() != null ? item.getItemUnitName() : "")
                .categoryName(item.getCategoryName() != null ? item.getCategoryName() : "-")
                .build();
    }

    /**
     * 格式化创建时间为邮件展示格式。
     */
    private String formatEmailCreateTime(Instant createTime) {
        if (createTime == null) {
            return "";
        }
        return createTime.atZone(ZONE_ID_SHANGHAI).format(EMAIL_DATE_FORMATTER);
    }

    /**
     * 格式化数量为邮件展示格式，去掉多余的小数位。
     * <p>例如 2.00 格式化为 2，2.10 格式化为 2.1。</p>
     */
    private String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    /**
     * 为需要放入冰箱但用户未填写存放位置的物品，调用 AI 补充存放位置。
     * <p>支持原有物品和本次新增物品；AI 失败时使用默认位置兜底，不影响结算主流程。</p>
     */
    private void fillMissingStorageLocations(List<PurchasePlanSettleItemRequest> settleItems,
                                             Map<Long, BizPurchasePlanItem> planItemMap) {
        List<StorageLocationSuggestRequest> suggestRequests = new ArrayList<>();
        List<PurchasePlanSettleItemRequest> itemsNeedLocation = new ArrayList<>();

        for (PurchasePlanSettleItemRequest settleItem : settleItems) {
            if (Boolean.TRUE.equals(settleItem.getSkip())) {
                continue;
            }
            if (settleItem.getStorageLocation() != null && !settleItem.getStorageLocation().isBlank()) {
                continue;
            }

            boolean shouldStore;
            String itemName;
            Long categoryId;
            if (settleItem.getPlanItemId() != null) {
                BizPurchasePlanItem planItem = planItemMap.get(settleItem.getPlanItemId());
                shouldStore = settleItem.getForceStoreInFridge() != null
                        ? settleItem.getForceStoreInFridge()
                        : planItem.getStoreInFridge();
                itemName = planItem.getItemName();
                categoryId = settleItem.getCategoryId() != null ? settleItem.getCategoryId() : planItem.getCategoryId();
            } else {
                shouldStore = settleItem.getForceStoreInFridge() != null ? settleItem.getForceStoreInFridge() : true;
                itemName = settleItem.getItemName();
                categoryId = settleItem.getCategoryId();
            }

            if (!Boolean.TRUE.equals(shouldStore)) {
                continue;
            }

            suggestRequests.add(StorageLocationSuggestRequest.builder()
                    .itemName(itemName)
                    .categoryName(resolveCategoryName(categoryId))
                    .shelfLifeDays(settleItem.getShelfLifeDays())
                    .build());
            itemsNeedLocation.add(settleItem);
        }

        if (suggestRequests.isEmpty()) {
            return;
        }

        List<String> suggestedLocations;
        try {
            suggestedLocations = purchaseIntelligenceService.suggestStorageLocations(suggestRequests);
        } catch (Exception e) {
            log.warn("AI 存放位置推荐失败，使用默认位置兜底", e);
            suggestedLocations = List.of();
        }

        for (int i = 0; i < itemsNeedLocation.size(); i++) {
            String location = i < suggestedLocations.size() ? suggestedLocations.get(i) : null;
            if (location == null || location.isBlank()) {
                location = defaultStorageLocationByCategoryName(suggestRequests.get(i).getCategoryName());
            }
            itemsNeedLocation.get(i).setStorageLocation(location);
        }
    }

    /**
     * 校验单个结算物品。
     */
    private void validateSettleItem(PurchasePlanSettleItemRequest settleItem,
                                    Map<Long, BizPurchasePlanItem> existingItemMap,
                                    Set<Long> validCategoryIds,
                                    Set<Long> validUnitIds) {
        if (settleItem.getPlanItemId() != null) {
            // 原有物品
            BizPurchasePlanItem existingItem = existingItemMap.get(settleItem.getPlanItemId());
            if (existingItem == null) {
                throw BusinessException.purchasePlanItemNotFound();
            }
            if (!Boolean.TRUE.equals(settleItem.getSkip())) {
                if (settleItem.getActualNum() == null || settleItem.getActualNum().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException(400, HttpStatus.BAD_REQUEST, "非跳过项的实际数量必须大于0");
                }
                Long categoryIdToValidate = settleItem.getCategoryId() != null ? settleItem.getCategoryId() : existingItem.getCategoryId();
                Long unitIdToValidate = settleItem.getItemUnitId() != null ? settleItem.getItemUnitId() : existingItem.getItemUnitId();
                validateItemCategoryAndUnit(categoryIdToValidate, unitIdToValidate, validCategoryIds, validUnitIds);
                if (settleItem.getItemName() != null && settleItem.getItemName().isBlank()) {
                    throw new BusinessException(400, HttpStatus.BAD_REQUEST, "物品名称不能为空");
                }
            }
        } else {
            // 新增物品
            if (Boolean.TRUE.equals(settleItem.getSkip())) {
                throw new BusinessException(400, HttpStatus.BAD_REQUEST, "新增物品不能跳过");
            }
            if (settleItem.getItemName() == null || settleItem.getItemName().isBlank()) {
                throw new BusinessException(400, HttpStatus.BAD_REQUEST, "新增物品名称不能为空");
            }
            if (settleItem.getCategoryId() == null) {
                throw new BusinessException(400, HttpStatus.BAD_REQUEST, "新增物品分类不能为空");
            }
            if (settleItem.getItemUnitId() == null) {
                throw new BusinessException(400, HttpStatus.BAD_REQUEST, "新增物品单位不能为空");
            }
            if (settleItem.getActualNum() == null || settleItem.getActualNum().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(400, HttpStatus.BAD_REQUEST, "新增物品实际数量必须大于0");
            }
            validateItemCategoryAndUnit(settleItem.getCategoryId(), settleItem.getItemUnitId(), validCategoryIds, validUnitIds);
        }
    }

    /**
     * 校验单个分类和单位是否有效。
     */
    private void validateItemCategoryAndUnit(Long categoryId, Long itemUnitId,
                                             Set<Long> validCategoryIds,
                                             Set<Long> validUnitIds) {
        if (categoryId != null && !validCategoryIds.contains(categoryId)) {
            throw BusinessException.categoryNotFound();
        }
        if (itemUnitId != null && !validUnitIds.contains(itemUnitId)) {
            throw BusinessException.unitNotFound();
        }
    }

    /**
     * 根据结算请求更新原有物品字段。
     */
    private void updateExistingItemFromSettle(BizPurchasePlanItem planItem,
                                              PurchasePlanSettleItemRequest settleItem) {
        if (settleItem.getItemName() != null && !settleItem.getItemName().isBlank()) {
            planItem.setItemName(settleItem.getItemName());
        }
        if (settleItem.getCategoryId() != null) {
            planItem.setCategoryId(settleItem.getCategoryId());
        }
        if (settleItem.getItemUnitId() != null) {
            planItem.setItemUnitId(settleItem.getItemUnitId());
        }
        if (settleItem.getForceStoreInFridge() != null) {
            planItem.setStoreInFridge(settleItem.getForceStoreInFridge());
        }
        planItem.setActualNum(settleItem.getActualNum());
        planItem.setProductionDate(settleItem.getProductionDate());
        planItem.setShelfLifeDays(settleItem.getShelfLifeDays());
        planItem.setStorageLocation(settleItem.getStorageLocation());
        planItem.setRemark(settleItem.getRemark());
    }

    /**
     * 根据结算请求创建新物品实体。
     */
    private BizPurchasePlanItem createNewPlanItemFromSettle(Long planId,
                                                            PurchasePlanSettleItemRequest settleItem,
                                                            Instant now) {
        BizPurchasePlanItem item = new BizPurchasePlanItem();
        item.setPlanId(planId);
        item.setItemName(settleItem.getItemName());
        item.setCategoryId(settleItem.getCategoryId());
        item.setPlannedNum(settleItem.getActualNum());
        item.setItemUnitId(settleItem.getItemUnitId());
        item.setStoreInFridge(settleItem.getForceStoreInFridge() != null ? settleItem.getForceStoreInFridge() : true);
        item.setActualNum(settleItem.getActualNum());
        item.setProductionDate(settleItem.getProductionDate());
        item.setShelfLifeDays(settleItem.getShelfLifeDays());
        item.setStorageLocation(settleItem.getStorageLocation());
        item.setRemark(settleItem.getRemark());
        item.setStatus(PurchasePlanItemStatus.PENDING.getCode());
        item.setCreateTime(now);
        item.setUpdateTime(now);
        return item;
    }

    /**
     * 根据分类ID查询分类名称（过滤已删除）。
     */
    private String resolveCategoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .filter(category -> !Boolean.TRUE.equals(category.getIsDeleted()))
                .map(BizItemCategory::getCategoryName)
                .orElse(null);
    }

    /**
     * 根据物品分类返回默认存放位置。
     */
    private String defaultStorageLocation(BizPurchasePlanItem item) {
        return defaultStorageLocationByCategoryName(resolveCategoryName(item.getCategoryId()));
    }

    /**
     * 根据分类名称返回默认存放位置。
     */
    private String defaultStorageLocationByCategoryName(String categoryName) {
        if (categoryName == null) {
            return "冷藏室";
        }
        String lower = categoryName.toLowerCase();
        if (lower.contains("肉") || lower.contains("海鲜") || lower.contains("鱼")
                || lower.contains("虾") || lower.contains("冻")) {
            return "冷冻室";
        }
        if (lower.contains("蔬") || lower.contains("果") || lower.contains("菜") || lower.contains("叶")) {
            return "冷藏室抽屉";
        }
        if (lower.contains("蛋") || lower.contains("奶") || lower.contains("乳")
                || lower.contains("饮") || lower.contains("汁")) {
            return "冷藏室";
        }
        return "冷藏室";
    }

    /**
     * 校验物品清单中的分类和单位。
     */
    private void validateItems(List<PurchasePlanItemCreateRequest> items) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        List<BizItemCategory> categories = categoryRepository.findAllByOwnerIdOrSystemDefault(currentUserId);
        List<BizItemUnit> units = unitRepository.findAllByOwnerIdOrSystemDefault(currentUserId);

        Set<Long> validCategoryIds = categories.stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .map(BizItemCategory::getId)
                .collect(Collectors.toSet());
        Set<Long> validUnitIds = units.stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .map(BizItemUnit::getId)
                .collect(Collectors.toSet());

        for (PurchasePlanItemCreateRequest item : items) {
            if (item.getCategoryId() != null && !validCategoryIds.contains(item.getCategoryId())) {
                throw BusinessException.categoryNotFound();
            }
            if (item.getItemUnitId() != null && !validUnitIds.contains(item.getItemUnitId())) {
                throw BusinessException.unitNotFound();
            }
        }
    }

    /**
     * 创建方案物品实体列表。
     */
    private List<BizPurchasePlanItem> createPlanItems(Long planId, List<PurchasePlanItemCreateRequest> items) {
        Instant now = Instant.now();
        List<BizPurchasePlanItem> result = new ArrayList<>();
        for (PurchasePlanItemCreateRequest request : items) {
            BizPurchasePlanItem item = new BizPurchasePlanItem();
            item.setPlanId(planId);
            item.setItemName(request.getItemName());
            item.setCategoryId(request.getCategoryId());
            item.setPlannedNum(request.getPlannedNum());
            item.setItemUnitId(request.getItemUnitId());
            item.setStoreInFridge(request.getStoreInFridge() != null ? request.getStoreInFridge() : true);
            item.setStatus(PurchasePlanItemStatus.PENDING.getCode());
            item.setCreateTime(now);
            item.setUpdateTime(now);
            result.add(item);
        }
        return result;
    }

    /**
     * 根据方案物品和核对信息创建库存物品。
     */
    private BizFridgeItem createFridgeItem(BizPurchasePlan plan, BizPurchasePlanItem planItem,
                                           PurchasePlanSettleItemRequest settleItem, Long operatorId) {
        BizFridgeItem item = new BizFridgeItem();
        item.setFridgeId(plan.getFridgeId());
        item.setItemName(planItem.getItemName());
        item.setItemUnitId(planItem.getItemUnitId());
        item.setCategoryId(planItem.getCategoryId());
        item.setItemNum(settleItem.getActualNum());
        item.setProductionDate(settleItem.getProductionDate());
        item.setShelfLifeDays(settleItem.getShelfLifeDays());
        item.setStorageLocation(settleItem.getStorageLocation());
        item.setRemark(settleItem.getRemark());
        item.setStoredDate(LocalDate.now());
        item.setOperatorId(operatorId);
        item.setIsDeleted(false);

        Instant now = Instant.now();
        item.setCreateTime(now);
        item.setUpdateTime(now);

        return item;
    }

    /**
     * 转换为 VO。
     */
    private PurchasePlanVO convertToVO(BizPurchasePlan plan, List<BizPurchasePlanItem> items,
                                       String fridgeName) {
        PurchasePlanSource source = PurchasePlanSource.fromCode(plan.getSource());

        List<PurchasePlanItemVO> itemVOs = items.stream()
                .map(this::convertItemToVO)
                .toList();

        long storedCount = itemVOs.stream()
                .filter(i -> PurchasePlanItemStatus.STORED.getCode() == i.getStatus())
                .count();
        long notStoredCount = itemVOs.stream()
                .filter(i -> PurchasePlanItemStatus.PURCHASED_NOT_STORED.getCode() == i.getStatus())
                .count();
        long skippedCount = itemVOs.stream()
                .filter(i -> PurchasePlanItemStatus.SKIPPED.getCode() == i.getStatus())
                .count();

        PurchasePlanVO.PurchasePlanVOBuilder builder = PurchasePlanVO.builder()
                .id(plan.getId())
                .fridgeId(plan.getFridgeId())
                .fridgeName(fridgeName)
                .planName(plan.getPlanName())
                .source(source.getCode())
                .sourceDesc(source.getDesc())
                .planStatus(plan.getPlanStatus())
                .sceneDesc(plan.getSceneDesc())
                .totalItems(plan.getTotalItems())
                .completedItems(plan.getCompletedItems())
                .storedCount((int) storedCount)
                .notStoredCount((int) notStoredCount)
                .skippedCount((int) skippedCount)
                .createTime(plan.getCreateTime())
                .updateTime(plan.getUpdateTime())
                .items(itemVOs);

        return builder.build();
    }

    private PurchasePlanItemVO convertItemToVO(BizPurchasePlanItem item) {
        String categoryName = null;
        if (item.getCategoryId() != null) {
            categoryName = categoryRepository.findById(item.getCategoryId())
                    .map(BizItemCategory::getCategoryName)
                    .orElse(null);
        }
        UnitInfo unitInfo = resolveUnitInfo(item.getItemUnitId());
        PurchasePlanItemStatus status = PurchasePlanItemStatus.fromCode(item.getStatus());

        return PurchasePlanItemVO.builder()
                .id(item.getId())
                .planId(item.getPlanId())
                .itemName(item.getItemName())
                .categoryId(item.getCategoryId())
                .categoryName(categoryName)
                .plannedNum(item.getPlannedNum())
                .itemUnitId(item.getItemUnitId())
                .itemUnitName(unitInfo != null ? unitInfo.getUnitName() : null)
                .unitTypeId(unitInfo != null ? unitInfo.getUnitTypeId() : null)
                .unitTypeName(unitInfo != null ? unitInfo.getUnitTypeName() : null)
                .actualNum(item.getActualNum())
                .productionDate(item.getProductionDate())
                .shelfLifeDays(item.getShelfLifeDays())
                .storageLocation(item.getStorageLocation())
                .status(item.getStatus())
                .statusDesc(status.getDesc())
                .storeInFridge(item.getStoreInFridge())
                .remark(item.getRemark())
                .build();
    }

    /**
     * 根据单位ID查询单位信息（含单位类型）。
     *
     * @param itemUnitId 单位ID
     * @return 单位信息，未找到或已删除则返回 null
     */
    private UnitInfo resolveUnitInfo(Long itemUnitId) {
        if (itemUnitId == null) {
            return null;
        }
        return unitRepository.findById(itemUnitId)
                .filter(unit -> !Boolean.TRUE.equals(unit.getIsDeleted()))
                .map(unit -> {
                    Long unitTypeId = unit.getUnitTypeId();
                    String unitTypeName = null;
                    if (unitTypeId != null) {
                        unitTypeName = unitTypeRepository.findById(unitTypeId)
                                .filter(type -> !Boolean.TRUE.equals(type.getIsDeleted()))
                                .map(BizUnitType::getUnitTypeName)
                                .orElse(null);
                    }
                    return new UnitInfo(unit.getUnitName(), unitTypeId, unitTypeName);
                })
                .orElse(null);
    }

    /**
     * 根据单位ID查询单位名称（过滤已删除）。
     *
     * @param itemUnitId 单位ID
     * @return 单位名称，未找到或已删除则返回 null
     */
    private String resolveUnitName(Long itemUnitId) {
        UnitInfo info = resolveUnitInfo(itemUnitId);
        return info != null ? info.getUnitName() : null;
    }

    /**
     * 单位信息内部类。
     */
    @Getter
    @AllArgsConstructor
    private static class UnitInfo {
        private final String unitName;
        private final Long unitTypeId;
        private final String unitTypeName;
    }
}
