package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.vo.ItemCategoryVO;
import com.yx.fridgebutler.dto.ItemCategoryCreateRequest;
import com.yx.fridgebutler.dto.ItemCategoryUpdateRequest;
import com.yx.fridgebutler.dto.ItemCreateRequest;
import com.yx.fridgebutler.vo.ItemVO;
import com.yx.fridgebutler.dto.ItemSearchRequest;
import com.yx.fridgebutler.dto.ItemTakeOutRequest;
import com.yx.fridgebutler.vo.ItemUnitVO;
import com.yx.fridgebutler.dto.ItemUpdateRequest;
import com.yx.fridgebutler.vo.UnitTypeVO;
import com.yx.fridgebutler.entity.BizFridge;
import com.yx.fridgebutler.entity.BizFridgeItem;
import com.yx.fridgebutler.entity.BizItemCategory;
import com.yx.fridgebutler.entity.BizItemUnit;
import com.yx.fridgebutler.entity.BizItemAddRecord;
import com.yx.fridgebutler.entity.BizItemChangeRecord;
import com.yx.fridgebutler.entity.BizItemTakeOutRecord;
import com.yx.fridgebutler.entity.BizUnitType;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.BizFridgeItemRepository;
import com.yx.fridgebutler.repository.BizFridgeRepository;
import com.yx.fridgebutler.repository.BizItemCategoryRepository;
import com.yx.fridgebutler.repository.BizItemUnitRepository;
import com.yx.fridgebutler.repository.BizItemAddRecordRepository;
import com.yx.fridgebutler.repository.BizItemChangeRecordRepository;
import com.yx.fridgebutler.repository.BizItemTakeOutRecordRepository;
import com.yx.fridgebutler.repository.BizUnitTypeRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 物品服务实现类，处理物品的创建、更新、查询、删除以及取出等业务逻辑。
 * <p>
 * 包含物品分类、单位、单位类型的查询，以及物品取出时的数量校验和取出记录保存。
 * 所有操作均基于当前登录用户进行权限校验。
 */
@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private BizFridgeItemRepository itemRepository;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private BizItemCategoryRepository categoryRepository;

    @Autowired
    private BizItemUnitRepository unitRepository;

    @Autowired
    private BizUnitTypeRepository unitTypeRepository;

    @Autowired
    private BizItemTakeOutRecordRepository takeOutRecordRepository;

    @Autowired
    private BizItemAddRecordRepository addRecordRepository;

    @Autowired
    private BizItemChangeRecordRepository changeRecordRepository;

    /**
     * {@inheritDoc}
     * <p>
     * 返回系统默认分类和当前用户自定义分类的合并列表。
     */
    @Override
    public List<ItemCategoryVO> listItemCategories() {
        Long currentUserId = getCurrentUserId();
        log.info("查询物品分类列表，用户ID：{}", currentUserId);

        List<BizItemCategory> categories = categoryRepository.findAllByOwnerIdOrSystemDefault(currentUserId);

        return categories.stream()
                .sorted(Comparator.comparing(BizItemCategory::getIsSystemDefault))
                .map(c -> ItemCategoryVO.builder()
                        .id(c.getId())
                        .categoryName(c.getCategoryName())
                        .isSystemDefault(c.getIsSystemDefault())
                        .build())
                .toList();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 查询指定用户可用的分类详情，只能查看系统默认分类或自己创建的分类。
     */
    @Override
    public ItemCategoryVO getItemCategory(Long id) {
        Long currentUserId = getCurrentUserId();
        log.info("查询物品分类详情，分类ID：{}，用户ID：{}", id, currentUserId);

        BizItemCategory category = categoryRepository.findById(id)
                .orElseThrow(BusinessException::categoryNotFound);

        if (Boolean.TRUE.equals(category.getIsDeleted())) {
            throw BusinessException.categoryNotFound();
        }

        // 只能查看系统默认分类或自己创建的分类
        if (Boolean.FALSE.equals(category.getIsSystemDefault()) && !category.getOwnerId().equals(currentUserId)) {
            throw BusinessException.categoryNotFound();
        }

        return ItemCategoryVO.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .isSystemDefault(category.getIsSystemDefault())
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 创建用户自定义分类，同一用户下分类名称不能重复。
     */
    @Override
    public Long createItemCategory(ItemCategoryCreateRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("创建物品分类，用户ID：{}，分类名称：{}", currentUserId, request.getCategoryName());

        // 校验同一用户下是否已存在相同名称的未删除分类
        if (categoryRepository.existsByCategoryNameAndOwnerIdAndIsDeletedFalse(request.getCategoryName(), currentUserId)) {
            throw BusinessException.categoryNameExists();
        }

        BizItemCategory category = new BizItemCategory();
        category.setCategoryName(request.getCategoryName());
        category.setOwnerId(currentUserId);
        category.setIsSystemDefault(false);
        category.setIsDeleted(false);
        Instant now = Instant.now();
        category.setCreateTime(now);
        category.setUpdateTime(now);

        BizItemCategory saved = categoryRepository.save(category);
        return saved.getId();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 只能更新自己创建的自定义分类，系统默认分类不允许编辑。
     */
    @Override
    public void updateItemCategory(ItemCategoryUpdateRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("更新物品分类，用户ID：{}，分类ID：{}", currentUserId, request.getId());

        BizItemCategory category = categoryRepository.findByIdAndOwnerIdAndIsDeletedFalse(request.getId(), currentUserId)
                .orElseThrow(BusinessException::categoryNotFound);

        if (Boolean.TRUE.equals(category.getIsSystemDefault())) {
            throw BusinessException.categoryNotEditable();
        }

        // 校验新名称是否与该用户下的其他分类重名
        if (!category.getCategoryName().equals(request.getCategoryName()) &&
                categoryRepository.existsByCategoryNameAndOwnerIdAndIsDeletedFalse(request.getCategoryName(), currentUserId)) {
            throw BusinessException.categoryNameExists();
        }

        category.setCategoryName(request.getCategoryName());
        category.setUpdateTime(Instant.now());
        categoryRepository.save(category);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 只能删除自己创建的自定义分类。删除后该分类下已关联的物品仍保留关联关系，
     * 但前端展示时会将分类名称显示为"未知"。
     */
    @Override
    @Transactional
    public void deleteItemCategory(Long id) {
        Long currentUserId = getCurrentUserId();
        log.info("删除物品分类，分类ID：{}，用户ID：{}", id, currentUserId);

        BizItemCategory category = categoryRepository.findByIdAndOwnerIdAndIsDeletedFalse(id, currentUserId)
                .orElseThrow(BusinessException::categoryNotFound);

        if (Boolean.TRUE.equals(category.getIsSystemDefault())) {
            throw BusinessException.categoryNotEditable();
        }

        category.setIsDeleted(true);
        category.setUpdateTime(Instant.now());
        categoryRepository.save(category);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 返回系统默认单位和当前用户自定义单位的合并列表，同时填充每个单位对应的单位类型名称。
     */
    @Override
    public List<ItemUnitVO> listItemUnits() {
        Long currentUserId = getCurrentUserId();
        log.info("查询物品单位列表，用户ID：{}", currentUserId);

        List<BizItemUnit> units = unitRepository.findAllByOwnerIdOrSystemDefault(currentUserId);

        // 收集所有单位类型ID，用于批量查询单位类型名称
        Set<Long> unitTypeIds = units.stream()
                .map(BizItemUnit::getUnitTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> unitTypeMap = unitTypeRepository.findAllById(unitTypeIds).stream()
                .collect(Collectors.toMap(BizUnitType::getId, BizUnitType::getUnitTypeName));

        return units.stream()
                .map(u -> ItemUnitVO.builder()
                        .id(u.getId())
                        .unitName(u.getUnitName())
                        .unitTypeId(u.getUnitTypeId())
                        .unitTypeName(unitTypeMap.get(u.getUnitTypeId()))
                        .isSystemDefault(u.getIsSystemDefault())
                        .build())
                .toList();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 返回系统默认单位类型和当前用户自定义单位类型的合并列表。
     */
    @Override
    public List<UnitTypeVO> listUnitTypes() {
        Long currentUserId = getCurrentUserId();
        log.info("查询单位类型列表，用户ID：{}", currentUserId);

        List<BizUnitType> unitTypes = unitTypeRepository.findAllByOwnerIdOrSystemDefault(currentUserId);

        return unitTypes.stream()
                .map(t -> UnitTypeVO.builder()
                        .id(t.getId())
                        .unitTypeName(t.getUnitTypeName())
                        .isSystemDefault(t.getIsSystemDefault())
                        .build())
                .toList();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 创建前会校验冰箱归属权、分类存在性（如传了分类ID）、单位存在性（如传了单位ID）。
     */
    @Override
    public Long createItem(ItemCreateRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("新增物品，用户ID：{}，冰箱ID：{}，物品名称：{}", currentUserId, request.getFridgeId(), request.getItemName());

        // 校验冰箱是否属于当前用户
        fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(request.getFridgeId(), currentUserId)
                .orElseThrow(BusinessException::fridgeNotFound);

        // 校验分类是否存在（如果传了分类ID）
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(BusinessException::categoryNotFound);
        }

        // 校验单位是否存在（如果传了单位ID）
        if (request.getItemUnitId() != null) {
            unitRepository.findById(request.getItemUnitId())
                    .orElseThrow(BusinessException::unitNotFound);
        }

        BizFridgeItem item = new BizFridgeItem();
        item.setFridgeId(request.getFridgeId());
        item.setItemName(request.getItemName());
        item.setItemUnitId(request.getItemUnitId());
        item.setStoredDate(request.getStoredDate());
        item.setProductionDate(request.getProductionDate());
        item.setShelfLifeDays(request.getShelfLifeDays());
        item.setCategoryId(request.getCategoryId());
        item.setItemNum(request.getItemNum());
        item.setRemark(request.getRemark());
        item.setOperatorId(currentUserId);
        item.setIsDeleted(false);

        Instant now = Instant.now();
        item.setCreateTime(now);
        item.setUpdateTime(now);

        BizFridgeItem saved = itemRepository.save(item);

        // 保存添加记录
        BizItemAddRecord addRecord = BizItemAddRecord.builder()
                .itemId(saved.getId())
                .fridgeId(saved.getFridgeId())
                .itemName(saved.getItemName())
                .addNum(saved.getItemNum())
                .remainingNum(saved.getItemNum())
                .operatorId(currentUserId)
                .createTime(Instant.now())
                .build();
        addRecordRepository.save(addRecord);
        log.info("保存添加记录成功，记录ID：{}，物品ID：{}", addRecord.getId(), saved.getId());

        return saved.getId();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 更新前会校验物品存在性、冰箱归属权、分类存在性（如传了分类ID）、单位存在性（如传了单位ID）。
     */
    @Override
    public void updateItem(ItemUpdateRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("更新物品，用户ID：{}，物品ID：{}" , currentUserId, request.getId());

        // 校验物品是否存在且未删除
        BizFridgeItem item = itemRepository.findById(request.getId())
                .orElseThrow(BusinessException::itemNotFound);
        if (Boolean.TRUE.equals(item.getIsDeleted())) {
            throw BusinessException.itemNotFound();
        }

        // 校验冰箱是否属于当前用户
        fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(item.getFridgeId(), currentUserId)
                .orElseThrow(BusinessException::fridgeNotFound);

        // 校验分类是否存在（如果传了分类ID）
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(BusinessException::categoryNotFound);
        }

        // 校验单位是否存在（如果传了单位ID）
        if (request.getItemUnitId() != null) {
            unitRepository.findById(request.getItemUnitId())
                    .orElseThrow(BusinessException::unitNotFound);
        }

        // 记录变更前的值，用于生成变更记录
        Long itemId = item.getId();
        Long fridgeId = item.getFridgeId();
        Instant now = Instant.now();

        saveChangeRecordIfDifferent(itemId, fridgeId, currentUserId, "UPDATE_NAME", "item_name", item.getItemName(), request.getItemName());
        saveChangeRecordIfDifferent(itemId, fridgeId, currentUserId, "UPDATE_UNIT", "item_unit_id", item.getItemUnitId(), request.getItemUnitId());
        saveChangeRecordIfDifferent(itemId, fridgeId, currentUserId, "UPDATE_STORED_DATE", "stored_date", item.getStoredDate(), request.getStoredDate());
        saveChangeRecordIfDifferent(itemId, fridgeId, currentUserId, "UPDATE_PRODUCTION_DATE", "production_date", item.getProductionDate(), request.getProductionDate());
        saveChangeRecordIfDifferent(itemId, fridgeId, currentUserId, "UPDATE_SHELF_LIFE", "shelf_life_days", item.getShelfLifeDays(), request.getShelfLifeDays());
        saveChangeRecordIfDifferent(itemId, fridgeId, currentUserId, "UPDATE_CATEGORY", "category_id", item.getCategoryId(), request.getCategoryId());
        saveChangeRecordIfDifferent(itemId, fridgeId, currentUserId, "UPDATE_NUM", "item_num", item.getItemNum(), request.getItemNum());
        saveChangeRecordIfDifferent(itemId, fridgeId, currentUserId, "UPDATE_REMARK", "remark", item.getRemark(), request.getRemark());

        item.setItemName(request.getItemName());
        item.setItemUnitId(request.getItemUnitId());
        item.setStoredDate(request.getStoredDate());
        item.setProductionDate(request.getProductionDate());
        item.setShelfLifeDays(request.getShelfLifeDays());
        item.setCategoryId(request.getCategoryId());
        item.setItemNum(request.getItemNum());
        item.setRemark(request.getRemark());
        item.setUpdateTime(now);

        itemRepository.save(item);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 采用软删除方式，删除前会校验物品存在性和冰箱归属权。
     */
    @Override
    @Transactional
    public void deleteItem(Long id) {
        Long currentUserId = getCurrentUserId();
        log.info("删除物品，物品ID：{}，用户ID：{}", id, currentUserId);

        // 校验物品是否存在且未删除
        BizFridgeItem item = itemRepository.findById(id)
                .orElseThrow(BusinessException::itemNotFound);
        if (Boolean.TRUE.equals(item.getIsDeleted())) {
            throw BusinessException.itemNotFound();
        }

        // 校验冰箱是否属于当前用户
        fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(item.getFridgeId(), currentUserId)
                .orElseThrow(BusinessException::fridgeNotFound);

        item.setIsDeleted(true);
        item.setUpdateTime(Instant.now());
        itemRepository.save(item);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 取出逻辑：
     * <ol>
     *   <li>校验物品存在性和冰箱归属权</li>
     *   <li>校验取出数量不能超过现有数量</li>
     *   <li>计算剩余数量：若剩余为零或负数则软删除物品，否则更新数量</li>
     *   <li>保存取出记录</li>
     * </ol>
     */
    @Override
    @Transactional
    public void takeOutItem(ItemTakeOutRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("取出物品，用户ID：{}，物品ID：{}，取出数量：{}", currentUserId, request.getId(), request.getTakeOutNum());

        // 校验物品是否存在且未删除
        BizFridgeItem item = itemRepository.findById(request.getId())
                .orElseThrow(BusinessException::itemNotFound);
        if (Boolean.TRUE.equals(item.getIsDeleted())) {
            throw BusinessException.itemNotFound();
        }

        // 校验冰箱是否属于当前用户
        fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(item.getFridgeId(), currentUserId)
                .orElseThrow(BusinessException::fridgeNotFound);

        // 校验取出数量不能超过现有数量
        if (request.getTakeOutNum().compareTo(item.getItemNum()) > 0) {
            throw BusinessException.takeOutNumExceed();
        }

        // 计算剩余数量
        BigDecimal remainingNum = item.getItemNum().subtract(request.getTakeOutNum());

        if (remainingNum.compareTo(BigDecimal.ZERO) <= 0) {
            // 取出数量大于等于现有数量，软删除该物品
            item.setIsDeleted(true);
            log.info("取出物品后数量归零，软删除物品，物品ID：{}", request.getId());
        } else {
            // 更新剩余数量
            item.setItemNum(remainingNum);
        }

        item.setUpdateTime(Instant.now());
        itemRepository.save(item);

        // 保存取出记录
        BizItemTakeOutRecord record = BizItemTakeOutRecord.builder()
                .itemId(item.getId())
                .fridgeId(item.getFridgeId())
                .itemName(item.getItemName())
                .takeOutNum(request.getTakeOutNum())
                .remainingNum(remainingNum.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO : remainingNum)
                .operatorId(currentUserId)
                .createTime(Instant.now())
                .build();
        takeOutRecordRepository.save(record);
        log.info("保存取出记录成功，记录ID：{}，物品ID：{}" , record.getId(), item.getId());
    }

    /**
     * {@inheritDoc}
     * <p>
     * 搜索逻辑：
     * <ul>
     *   <li>若指定了冰箱ID，则在该冰箱下搜索，并校验冰箱归属权</li>
     *   <li>若未指定冰箱ID，则在当前用户所有冰箱下搜索</li>
     * </ul>
     */
    @Override
    public List<ItemVO> searchItems(ItemSearchRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("搜索物品，用户ID：{}，冰箱ID：{}，关键字：{}，分类ID：{}，单位ID：{}，单位类型ID：{}，排序：{} {}",
                currentUserId, request.getFridgeId(), request.getKeyword(), request.getCategoryId(),
                request.getUnitId(), request.getUnitTypeId(), request.getSortField(), request.getSortOrder());

        String likeKeyword = (request.getKeyword() == null || request.getKeyword().isBlank())
                ? "" : "%" + request.getKeyword() + "%";

        Sort sort = buildSort(request.getSortField(), request.getSortOrder());

        List<BizFridgeItem> items;
        if (request.getFridgeId() != null) {
            // 校验冰箱是否属于当前用户
            fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(request.getFridgeId(), currentUserId)
                    .orElseThrow(BusinessException::fridgeNotFound);
            items = itemRepository.searchItemsByFridgeId(
                    request.getFridgeId(), likeKeyword, request.getCategoryId(), request.getUnitId(), request.getUnitTypeId(), sort);
        } else {
            // 获取当前用户所有冰箱ID
            List<Long> fridgeIds = fridgeRepository.findByOwnerIdAndIsDeletedFalse(currentUserId, Sort.unsorted())
                    .stream()
                    .map(BizFridge::getId)
                    .toList();

            if (fridgeIds.isEmpty()) {
                return List.of();
            }

            items = itemRepository.searchItems(
                    fridgeIds, likeKeyword, request.getCategoryId(), request.getUnitId(), request.getUnitTypeId(), sort);
        }

        return convertToVOList(items);
    }

    /**
     * 构建排序对象。
     *
     * @param sortField 排序字段（itemNum/storedDate）
     * @param sortOrder 排序方向（asc/desc）
     * @return Spring Data的Sort对象
     */
    private static Sort buildSort(String sortField, String sortOrder) {
        if (sortField == null) {
            sortField = "storedDate";
        }

        String field = switch (sortField) {
            case "itemNum" -> "itemNum";
            case "storedDate" -> "storedDate";
            default -> throw BusinessException.unknownSortField();
        };

        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }

    /**
     * 将物品实体列表转换为视图对象列表。
     * <p>
     * 采用批量查询策略，先收集所有分类ID、单位ID、单位类型ID，再统一查询对应名称，减少数据库交互次数。
     *
     * @param items 物品实体列表
     * @return 物品视图对象列表
     */
    private List<ItemVO> convertToVOList(List<BizFridgeItem> items) {
        // 批量查询分类名称
        Set<Long> categoryIds = items.stream()
                .map(BizFridgeItem::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> categoryMap = categoryRepository.findAllById(categoryIds).stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .collect(Collectors.toMap(BizItemCategory::getId, BizItemCategory::getCategoryName));

        // 批量查询单位信息
        Set<Long> unitIds = items.stream()
                .map(BizFridgeItem::getItemUnitId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, BizItemUnit> unitMap = unitRepository.findAllById(unitIds).stream()
                .collect(Collectors.toMap(BizItemUnit::getId, Function.identity()));

        // 批量查询单位类型名称
        Set<Long> unitTypeIds = unitMap.values().stream()
                .map(BizItemUnit::getUnitTypeId)
                .collect(Collectors.toSet());
        Map<Long, String> unitTypeMap = unitTypeRepository.findAllById(unitTypeIds).stream()
                .collect(Collectors.toMap(BizUnitType::getId, BizUnitType::getUnitTypeName));

        return items.stream()
                .map(item -> convertToVO(item, categoryMap, unitMap, unitTypeMap))
                .toList();
    }

    /**
     * 将单个物品实体转换为视图对象。
     *
     * @param item        物品实体
     * @param categoryMap 分类ID到名称的映射
     * @param unitMap     单位ID到单位实体的映射
     * @param unitTypeMap 单位类型ID到名称的映射
     * @return 物品视图对象
     */
    private ItemVO convertToVO(BizFridgeItem item,
                                  Map<Long, String> categoryMap,
                                  Map<Long, BizItemUnit> unitMap,
                                  Map<Long, String> unitTypeMap) {
        BizItemUnit unit = item.getItemUnitId() != null ? unitMap.get(item.getItemUnitId()) : null;
        Long unitTypeId = unit != null ? unit.getUnitTypeId() : null;
        String unitTypeName = unitTypeId != null ? unitTypeMap.get(unitTypeId) : null;

        return ItemVO.builder()
                .id(item.getId())
                .fridgeId(item.getFridgeId())
                .itemName(item.getItemName())
                .itemUnitId(item.getItemUnitId())
                .unitName(unit != null ? unit.getUnitName() : null)
                .unitTypeId(unitTypeId)
                .unitTypeName(unitTypeName)
                .storedDate(item.getStoredDate())
                .productionDate(item.getProductionDate())
                .shelfLifeDays(item.getShelfLifeDays())
                .categoryId(item.getCategoryId())
                .categoryName(resolveCategoryName(item.getCategoryId(), categoryMap))
                .itemNum(item.getItemNum())
                .remark(item.getRemark())
                .createTime(formatInstant(item.getCreateTime()))
                .updateTime(formatInstant(item.getUpdateTime()))
                .build();
    }

    /**
     * 解析物品的分类名称。
     * <p>
     * 如果分类 ID 为空，则返回 null；
     * 如果分类已被软删除（在 categoryMap 中不存在），则返回"未知"；
     * 否则返回分类原始名称。
     *
     * @param categoryId  分类ID
     * @param categoryMap 分类ID到名称的映射（仅包含未删除的分类）
     * @return 分类名称
     */
    private String resolveCategoryName(Long categoryId, Map<Long, String> categoryMap) {
        if (categoryId == null) {
            return null;
        }
        String name = categoryMap.get(categoryId);
        return name != null ? name : "未知";
    }

    /**
     * 如果字段值发生变化，则保存变更记录。
     *
     * @param itemId     物品ID
     * @param fridgeId   冰箱ID
     * @param operatorId 操作人ID
     * @param changeType 变更类型
     * @param fieldName  变更字段名
     * @param oldValue   变更前值
     * @param newValue   变更后值
     */
    private void saveChangeRecordIfDifferent(Long itemId, Long fridgeId, Long operatorId,
                                             String changeType, String fieldName,
                                             Object oldValue, Object newValue) {
        String oldStr = oldValue == null ? null : oldValue.toString();
        String newStr = newValue == null ? null : newValue.toString();
        if (!Objects.equals(oldStr, newStr)) {
            BizItemChangeRecord record = BizItemChangeRecord.builder()
                    .itemId(itemId)
                    .fridgeId(fridgeId)
                    .changeType(changeType)
                    .fieldName(fieldName)
                    .oldValue(oldStr)
                    .newValue(newStr)
                    .operatorId(operatorId)
                    .createTime(Instant.now())
                    .build();
            changeRecordRepository.save(record);
            log.info("保存变更记录成功，物品ID：{}，字段：{}，旧值：{}，新值：{}", itemId, fieldName, oldStr, newStr);
        }
    }

    /**
     * 将Instant格式化为上海时区的日期时间字符串。
     *
     * @param instant 时间戳
     * @return 格式化后的字符串，若传入null则返回null
     */
    private String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZONE_ID_SHANGHAI).format(DATE_TIME_FORMATTER);
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
     * 从Spring Security上下文中获取当前登录用户名。
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
