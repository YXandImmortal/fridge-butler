package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.purchase.*;
import com.yx.fridgebutler.entity.BizFridge;
import com.yx.fridgebutler.entity.BizPurchasePlan;
import com.yx.fridgebutler.entity.BizPurchasePlanItem;
import com.yx.fridgebutler.entity.BizPurchasePlanTemplate;
import com.yx.fridgebutler.entity.BizPurchasePlanTemplateItem;
import com.yx.fridgebutler.entity.BizUnitType;
import com.yx.fridgebutler.enums.PurchasePlanSource;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.BizFridgeRepository;
import com.yx.fridgebutler.repository.BizItemUnitRepository;
import com.yx.fridgebutler.repository.BizPurchasePlanItemRepository;
import com.yx.fridgebutler.repository.BizPurchasePlanRepository;
import com.yx.fridgebutler.repository.BizPurchasePlanTemplateItemRepository;
import com.yx.fridgebutler.repository.BizPurchasePlanTemplateRepository;
import com.yx.fridgebutler.repository.BizUnitTypeRepository;
import com.yx.fridgebutler.service.PurchasePlanService;
import com.yx.fridgebutler.service.PurchasePlanTemplateService;
import com.yx.fridgebutler.util.UserContextUtil;
import com.yx.fridgebutler.vo.purchase.PurchasePlanTemplateItemVO;
import com.yx.fridgebutler.vo.purchase.PurchasePlanTemplateVO;
import com.yx.fridgebutler.vo.purchase.PurchasePlanVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户采购计划模板服务实现。
 */
@Slf4j
@Service
public class PurchasePlanTemplateServiceImpl implements PurchasePlanTemplateService {

    /** 每个用户最多保存的模板数量。 */
    private static final int MAX_TEMPLATES_PER_USER = 10;

    @Autowired
    private BizPurchasePlanTemplateRepository templateRepository;

    @Autowired
    private BizPurchasePlanTemplateItemRepository templateItemRepository;

    @Autowired
    private BizPurchasePlanRepository planRepository;

    @Autowired
    private BizPurchasePlanItemRepository planItemRepository;

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private PurchasePlanService purchasePlanService;

    @Autowired
    private BizItemUnitRepository unitRepository;

    @Autowired
    private BizUnitTypeRepository unitTypeRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchasePlanTemplateVO createTemplate(PurchasePlanTemplateCreateRequest request) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        log.info("创建采购计划模板，用户ID：{}，模板名称：{}", currentUserId, request.getTemplateName());

        validateTemplateCreate(currentUserId, request.getTemplateName(), request.getItems().size());

        BizPurchasePlanTemplate template = new BizPurchasePlanTemplate();
        template.setUserId(currentUserId);
        template.setTemplateName(request.getTemplateName());
        template.setSceneDesc(request.getSceneDesc());
        template.setItemCount(request.getItems().size());

        Instant now = Instant.now();
        template.setCreateTime(now);
        template.setUpdateTime(now);

        BizPurchasePlanTemplate savedTemplate = templateRepository.save(template);

        List<BizPurchasePlanTemplateItem> items = createTemplateItems(savedTemplate.getId(), request.getItems());
        templateItemRepository.saveAll(items);

        return convertToVO(savedTemplate, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchasePlanTemplateVO savePlanAsTemplate(SavePlanAsTemplateRequest request) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        log.info("保存计划为模板，用户ID：{}，方案ID：{}，模板名称：{}",
                currentUserId, request.getPlanId(), request.getTemplateName());

        // 校验方案归属
        BizPurchasePlan plan = planRepository.findByIdAndUserId(request.getPlanId(), currentUserId)
                .orElseThrow(BusinessException::purchasePlanNotFound);

        // 获取方案物品
        List<BizPurchasePlanItem> planItems = planItemRepository.findByPlanId(request.getPlanId());
        if (planItems.isEmpty()) {
            throw BusinessException.purchasePlanEmptyItems();
        }

        validateTemplateCreate(currentUserId, request.getTemplateName(), planItems.size());

        // 创建模板
        BizPurchasePlanTemplate template = new BizPurchasePlanTemplate();
        template.setUserId(currentUserId);
        template.setTemplateName(request.getTemplateName());
        template.setSceneDesc(plan.getSceneDesc());
        template.setItemCount(planItems.size());

        Instant now = Instant.now();
        template.setCreateTime(now);
        template.setUpdateTime(now);

        BizPurchasePlanTemplate savedTemplate = templateRepository.save(template);

        // 复制方案物品到模板物品
        List<BizPurchasePlanTemplateItem> items = planItems.stream()
                .map(planItem -> {
                    BizPurchasePlanTemplateItem item = new BizPurchasePlanTemplateItem();
                    item.setTemplateId(savedTemplate.getId());
                    item.setItemName(planItem.getItemName());
                    item.setCategoryId(planItem.getCategoryId());
                    item.setPlannedNum(planItem.getPlannedNum());
                    item.setItemUnitId(planItem.getItemUnitId());
                    item.setStoreInFridge(planItem.getStoreInFridge());
                    item.setSortOrder(0);
                    item.setCreateTime(now);
                    item.setUpdateTime(now);
                    return item;
                })
                .toList();
        templateItemRepository.saveAll(items);

        return convertToVO(savedTemplate, items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchasePlanTemplateVO updateTemplate(Long id, PurchasePlanTemplateUpdateRequest request) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        log.info("修改采购计划模板，用户ID：{}，模板ID：{}", currentUserId, id);

        BizPurchasePlanTemplate template = templateRepository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(BusinessException::purchasePlanTemplateNotFound);

        // 校验名称唯一（排除自身）
        if (!template.getTemplateName().equals(request.getTemplateName())
                && templateRepository.existsByUserIdAndTemplateName(currentUserId, request.getTemplateName())) {
            throw BusinessException.purchasePlanTemplateNameExists();
        }

        // 删除旧物品
        List<BizPurchasePlanTemplateItem> oldItems = templateItemRepository.findByTemplateId(id);
        templateItemRepository.deleteAll(oldItems);

        // 保存新物品
        List<BizPurchasePlanTemplateItem> newItems = createTemplateItems(id, request.getItems());
        templateItemRepository.saveAll(newItems);

        // 更新模板
        template.setTemplateName(request.getTemplateName());
        template.setSceneDesc(request.getSceneDesc());
        template.setItemCount(request.getItems().size());
        template.setUpdateTime(Instant.now());
        templateRepository.save(template);

        return convertToVO(template, newItems);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long id) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        log.info("删除采购计划模板，用户ID：{}，模板ID：{}", currentUserId, id);

        BizPurchasePlanTemplate template = templateRepository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(BusinessException::purchasePlanTemplateNotFound);

        List<BizPurchasePlanTemplateItem> items = templateItemRepository.findByTemplateId(id);
        templateItemRepository.deleteAll(items);
        templateRepository.delete(template);
    }

    @Override
    public PurchasePlanTemplateVO getTemplate(Long id) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        BizPurchasePlanTemplate template = templateRepository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(BusinessException::purchasePlanTemplateNotFound);

        List<BizPurchasePlanTemplateItem> items = templateItemRepository.findByTemplateId(id);
        return convertToVO(template, items);
    }

    @Override
    public List<PurchasePlanTemplateVO> listTemplates() {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        List<BizPurchasePlanTemplate> templates = templateRepository.findByUserId(currentUserId, sort);

        return templates.stream()
                .map(template -> {
                    List<BizPurchasePlanTemplateItem> items = templateItemRepository.findByTemplateId(template.getId());
                    return convertToVO(template, items);
                })
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchasePlanVO useTemplate(Long templateId, UseTemplateRequest request) {
        Long currentUserId = UserContextUtil.getCurrentUserId();
        log.info("使用模板创建采购方案，用户ID：{}，模板ID：{}，冰箱ID：{}",
                currentUserId, templateId, request.getFridgeId());

        // 校验模板归属
        BizPurchasePlanTemplate template = templateRepository.findByIdAndUserId(templateId, currentUserId)
                .orElseThrow(BusinessException::purchasePlanTemplateNotFound);

        // 校验冰箱归属
        BizFridge fridge = fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(request.getFridgeId(), currentUserId)
                .orElseThrow(BusinessException::fridgeNotFound);

        // 获取模板物品
        List<BizPurchasePlanTemplateItem> templateItems = templateItemRepository.findByTemplateId(templateId);

        // 构建创建方案请求
        PurchasePlanCreateRequest planRequest = new PurchasePlanCreateRequest();
        planRequest.setFridgeId(request.getFridgeId());
        planRequest.setPlanName(request.getPlanName() != null && !request.getPlanName().isBlank()
                ? request.getPlanName()
                : template.getTemplateName());
        planRequest.setSceneDesc(template.getSceneDesc());

        List<PurchasePlanItemCreateRequest> items = templateItems.stream()
                .map(templateItem -> {
                    PurchasePlanItemCreateRequest item = new PurchasePlanItemCreateRequest();
                    item.setItemName(templateItem.getItemName());
                    item.setCategoryId(templateItem.getCategoryId());
                    item.setPlannedNum(templateItem.getPlannedNum());
                    item.setItemUnitId(templateItem.getItemUnitId());
                    item.setStoreInFridge(templateItem.getStoreInFridge());
                    return item;
                })
                .toList();
        planRequest.setItems(items);

        return purchasePlanService.createPlan(planRequest, PurchasePlanSource.TEMPLATE);
    }

    /**
     * 校验模板创建条件。
     */
    private void validateTemplateCreate(Long userId, String templateName, int itemCount) {
        if (itemCount <= 0) {
            throw BusinessException.purchasePlanEmptyItems();
        }
        if (templateRepository.countByUserId(userId) >= MAX_TEMPLATES_PER_USER) {
            throw BusinessException.purchasePlanTemplateLimitReached();
        }
        if (templateRepository.existsByUserIdAndTemplateName(userId, templateName)) {
            throw BusinessException.purchasePlanTemplateNameExists();
        }
    }

    /**
     * 创建模板物品实体列表。
     */
    private List<BizPurchasePlanTemplateItem> createTemplateItems(Long templateId, List<PurchasePlanItemCreateRequest> items) {
        Instant now = Instant.now();
        List<BizPurchasePlanTemplateItem> result = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            PurchasePlanItemCreateRequest request = items.get(i);
            BizPurchasePlanTemplateItem item = new BizPurchasePlanTemplateItem();
            item.setTemplateId(templateId);
            item.setItemName(request.getItemName());
            item.setCategoryId(request.getCategoryId());
            item.setPlannedNum(request.getPlannedNum());
            item.setItemUnitId(request.getItemUnitId());
            item.setStoreInFridge(request.getStoreInFridge() != null ? request.getStoreInFridge() : true);
            item.setSortOrder(i);
            item.setCreateTime(now);
            item.setUpdateTime(now);
            result.add(item);
        }
        return result;
    }

    /**
     * 转换为 VO。
     */
    private PurchasePlanTemplateVO convertToVO(BizPurchasePlanTemplate template, List<BizPurchasePlanTemplateItem> items) {
        List<PurchasePlanTemplateItemVO> itemVOs = items.stream()
                .map(this::convertItemToVO)
                .toList();

        return PurchasePlanTemplateVO.builder()
                .id(template.getId())
                .templateName(template.getTemplateName())
                .sceneDesc(template.getSceneDesc())
                .itemCount(template.getItemCount())
                .createTime(template.getCreateTime())
                .updateTime(template.getUpdateTime())
                .items(itemVOs)
                .build();
    }

    private PurchasePlanTemplateItemVO convertItemToVO(BizPurchasePlanTemplateItem item) {
        UnitInfo unitInfo = resolveUnitInfo(item.getItemUnitId());

        return PurchasePlanTemplateItemVO.builder()
                .id(item.getId())
                .templateId(item.getTemplateId())
                .itemName(item.getItemName())
                .categoryId(item.getCategoryId())
                .plannedNum(item.getPlannedNum())
                .itemUnitId(item.getItemUnitId())
                .itemUnitName(unitInfo != null ? unitInfo.getUnitName() : null)
                .unitTypeId(unitInfo != null ? unitInfo.getUnitTypeId() : null)
                .unitTypeName(unitInfo != null ? unitInfo.getUnitTypeName() : null)
                .sortOrder(item.getSortOrder())
                .storeInFridge(item.getStoreInFridge())
                .build();
    }

    /**
     * 根据单位ID查询单位信息（含单位类型）。
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
