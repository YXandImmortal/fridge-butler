package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.vo.ItemCategoryVO;
import com.yx.fridgebutler.dto.category.ItemCategoryCreateRequest;
import com.yx.fridgebutler.dto.category.ItemCategoryUpdateRequest;
import com.yx.fridgebutler.dto.item.ItemCreateRequest;
import com.yx.fridgebutler.vo.ItemVO;
import com.yx.fridgebutler.dto.item.ItemSearchRequest;
import com.yx.fridgebutler.dto.item.ItemTakeOutRequest;
import com.yx.fridgebutler.vo.ItemUnitVO;
import com.yx.fridgebutler.dto.unit.ItemUnitCreateRequest;
import com.yx.fridgebutler.dto.unit.ItemUnitUpdateRequest;
import com.yx.fridgebutler.dto.item.ItemUpdateRequest;
import com.yx.fridgebutler.vo.ExpiringSummaryVO;
import com.yx.fridgebutler.vo.TakeOutDailyStatisticsVO;
import com.yx.fridgebutler.vo.UnitTypeVO;
import com.yx.fridgebutler.dto.unittype.UnitTypeCreateRequest;
import com.yx.fridgebutler.dto.unittype.UnitTypeUpdateRequest;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

    /** 上海时区，用于时间格式化。 */
    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    /** 日期时间格式化器，格式为 yyyy-MM-dd HH:mm:ss。 */
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

        // 收集所有单位类型ID，用于批量查询单位类型名称（过滤已删除）
        Set<Long> unitTypeIds = units.stream()
                .map(BizItemUnit::getUnitTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> unitTypeMap = unitTypeRepository.findAllById(unitTypeIds).stream()
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .collect(Collectors.toMap(BizUnitType::getId, BizUnitType::getUnitTypeName));

        return units.stream()
                .map(u -> {
                    String typeName = unitTypeMap.get(u.getUnitTypeId());
                    return ItemUnitVO.builder()
                            .id(u.getId())
                            .unitName(u.getUnitName())
                            .unitTypeId(u.getUnitTypeId())
                            .unitTypeName(typeName != null ? typeName : "未知")
                            .isSystemDefault(u.getIsSystemDefault())
                            .build();
                })
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
     * 创建用户自定义单位类型，同一用户下单位类型名称不能重复。
     */
    @Override
    public Long createUnitType(UnitTypeCreateRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("创建单位类型，用户ID：{}，类型名称：{}", currentUserId, request.getTypeName());

        // 校验同一用户下是否已存在相同名称的未删除单位类型
        if (unitTypeRepository.existsByUnitTypeNameAndOwnerIdAndIsDeletedFalse(request.getTypeName(), currentUserId)) {
            throw BusinessException.unitTypeNameExists();
        }

        BizUnitType unitType = new BizUnitType();
        unitType.setUnitTypeName(request.getTypeName());
        unitType.setOwnerId(currentUserId);
        unitType.setIsSystemDefault(false);
        unitType.setIsDeleted(false);
        Instant now = Instant.now();
        unitType.setCreateTime(now);
        unitType.setUpdateTime(now);

        BizUnitType saved = unitTypeRepository.save(unitType);
        return saved.getId();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 只能更新自己创建的自定义单位类型，系统默认单位类型不允许编辑。
     */
    @Override
    public void updateUnitType(UnitTypeUpdateRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("更新单位类型，用户ID：{}，类型ID：{}", currentUserId, request.getId());

        BizUnitType unitType = unitTypeRepository.findByIdAndOwnerIdAndIsDeletedFalse(request.getId(), currentUserId)
                .orElseThrow(BusinessException::unitTypeNotFound);

        if (Boolean.TRUE.equals(unitType.getIsSystemDefault())) {
            throw BusinessException.unitTypeNotEditable();
        }

        // 校验新名称是否与该用户下的其他单位类型重名
        if (!unitType.getUnitTypeName().equals(request.getTypeName()) &&
                unitTypeRepository.existsByUnitTypeNameAndOwnerIdAndIsDeletedFalse(request.getTypeName(), currentUserId)) {
            throw BusinessException.unitTypeNameExists();
        }

        unitType.setUnitTypeName(request.getTypeName());
        unitType.setUpdateTime(Instant.now());
        unitTypeRepository.save(unitType);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 只能删除自己创建的自定义单位类型。删除后该类型下已关联的单位仍保留关联关系，
     * 但前端展示时会将单位类型名称显示为"未知"。
     */
    @Override
    @Transactional
    public void deleteUnitType(Long id) {
        Long currentUserId = getCurrentUserId();
        log.info("删除单位类型，类型ID：{}，用户ID：{}", id, currentUserId);

        BizUnitType unitType = unitTypeRepository.findByIdAndOwnerIdAndIsDeletedFalse(id, currentUserId)
                .orElseThrow(BusinessException::unitTypeNotFound);

        if (Boolean.TRUE.equals(unitType.getIsSystemDefault())) {
            throw BusinessException.unitTypeNotEditable();
        }

        unitType.setIsDeleted(true);
        unitType.setUpdateTime(Instant.now());
        unitTypeRepository.save(unitType);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 创建用户自定义单位，会校验单位类型是否存在，同一用户下单位名称不能重复。
     */
    @Override
    public Long createItemUnit(ItemUnitCreateRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("创建物品单位，用户ID：{}，单位名称：{}，单位类型ID：{}", currentUserId, request.getUnitName(), request.getUnitTypeId());

        // 校验单位类型是否存在
        BizUnitType unitType = unitTypeRepository.findById(request.getUnitTypeId())
                .orElseThrow(BusinessException::unitTypeNotFound);
        if (Boolean.TRUE.equals(unitType.getIsDeleted())) {
            throw BusinessException.unitTypeNotFound();
        }
        // 只能使用系统默认类型或自己创建的类型
        if (Boolean.FALSE.equals(unitType.getIsSystemDefault()) && !unitType.getOwnerId().equals(currentUserId)) {
            throw BusinessException.unitTypeNotFound();
        }

        // 校验同一用户下是否已存在相同名称的未删除单位
        if (unitRepository.existsByUnitNameAndOwnerIdAndIsDeletedFalse(request.getUnitName(), currentUserId)) {
            throw BusinessException.unitNameExists();
        }

        BizItemUnit unit = new BizItemUnit();
        unit.setUnitName(request.getUnitName());
        unit.setUnitTypeId(request.getUnitTypeId());
        unit.setOwnerId(currentUserId);
        unit.setIsSystemDefault(false);
        unit.setIsDeleted(false);
        Instant now = Instant.now();
        unit.setCreateTime(now);
        unit.setUpdateTime(now);

        BizItemUnit saved = unitRepository.save(unit);
        return saved.getId();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 只能更新自己创建的自定义单位，系统默认单位不允许编辑。
     */
    @Override
    public void updateItemUnit(ItemUnitUpdateRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("更新物品单位，用户ID：{}，单位ID：{}", currentUserId, request.getId());

        BizItemUnit unit = unitRepository.findByIdAndOwnerIdAndIsDeletedFalse(request.getId(), currentUserId)
                .orElseThrow(BusinessException::unitNotFound);

        if (Boolean.TRUE.equals(unit.getIsSystemDefault())) {
            throw BusinessException.unitNotEditable();
        }

        // 校验单位类型是否存在
        BizUnitType unitType = unitTypeRepository.findById(request.getUnitTypeId())
                .orElseThrow(BusinessException::unitTypeNotFound);
        if (Boolean.TRUE.equals(unitType.getIsDeleted())) {
            throw BusinessException.unitTypeNotFound();
        }
        // 只能使用系统默认类型或自己创建的类型
        if (Boolean.FALSE.equals(unitType.getIsSystemDefault()) && !unitType.getOwnerId().equals(currentUserId)) {
            throw BusinessException.unitTypeNotFound();
        }

        // 校验新名称是否与该用户下的其他单位重名
        if (!unit.getUnitName().equals(request.getUnitName()) &&
                unitRepository.existsByUnitNameAndOwnerIdAndIsDeletedFalse(request.getUnitName(), currentUserId)) {
            throw BusinessException.unitNameExists();
        }

        unit.setUnitName(request.getUnitName());
        unit.setUnitTypeId(request.getUnitTypeId());
        unit.setUpdateTime(Instant.now());
        unitRepository.save(unit);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 只能删除自己创建的自定义单位。删除后已关联该单位的物品仍保留关联关系，
     * 但前端展示时会将单位名称显示为"未知"。
     */
    @Override
    @Transactional
    public void deleteItemUnit(Long id) {
        Long currentUserId = getCurrentUserId();
        log.info("删除物品单位，单位ID：{}，用户ID：{}", id, currentUserId);

        BizItemUnit unit = unitRepository.findByIdAndOwnerIdAndIsDeletedFalse(id, currentUserId)
                .orElseThrow(BusinessException::unitNotFound);

        if (Boolean.TRUE.equals(unit.getIsSystemDefault())) {
            throw BusinessException.unitNotEditable();
        }

        unit.setIsDeleted(true);
        unit.setUpdateTime(Instant.now());
        unitRepository.save(unit);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 创建前会校验冰箱归属权、分类存在性（如传了分类ID）、单位存在性（如传了单位ID）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createItem(ItemCreateRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("新增物品，用户ID：{}，冰箱ID：{}，物品名称：{}", currentUserId, request.getFridgeId(), request.getItemName());

        // 校验冰箱是否属于当前用户
        fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(request.getFridgeId(), currentUserId)
                .orElseThrow(BusinessException::fridgeNotFound);

        // 校验分类是否存在且未删除（如果传了分类ID）
        if (request.getCategoryId() != null) {
            BizItemCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(BusinessException::categoryNotFound);
            if (Boolean.TRUE.equals(category.getIsDeleted())) {
                throw BusinessException.categoryNotFound();
            }
        }

        // 校验单位是否存在且未删除（如果传了单位ID）
        if (request.getItemUnitId() != null) {
            BizItemUnit unit = unitRepository.findById(request.getItemUnitId())
                    .orElseThrow(BusinessException::unitNotFound);
            if (Boolean.TRUE.equals(unit.getIsDeleted())) {
                throw BusinessException.unitNotFound();
            }
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
    @Transactional(rollbackFor = Exception.class)
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

        // 校验分类是否存在且未删除（如果传了分类ID）
        if (request.getCategoryId() != null) {
            BizItemCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(BusinessException::categoryNotFound);
            if (Boolean.TRUE.equals(category.getIsDeleted())) {
                throw BusinessException.categoryNotFound();
            }
        }

        // 校验单位是否存在且未删除（如果传了单位ID）
        if (request.getItemUnitId() != null) {
            BizItemUnit unit = unitRepository.findById(request.getItemUnitId())
                    .orElseThrow(BusinessException::unitNotFound);
            if (Boolean.TRUE.equals(unit.getIsDeleted())) {
                throw BusinessException.unitNotFound();
            }
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
     * 查询当前用户近30天每日取出次数，按日期升序返回，无数据日期补0。
     */
    @Override
    public List<TakeOutDailyStatisticsVO> getRecent30DaysTakeOutStatistics(Long fridgeId) {
        Long currentUserId = getCurrentUserId();
        log.info("查询近30天取出统计，用户ID：{}，冰箱ID：{}", currentUserId, fridgeId);

        // 计算30天前的时间点（上海时区）
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
        LocalDate startDate = today.minusDays(29);
        Instant startTime = startDate.atStartOfDay(ZONE_ID_SHANGHAI).toInstant();

        // 查询数据库中有记录的日期及次数
        List<Object[]> dbResults = takeOutRecordRepository.countDailyByOperatorIdAndTimeRange(
                currentUserId, fridgeId, startTime);

        // 将查询结果转为 Map<日期, 次数>
        Map<LocalDate, Long> countMap = dbResults.stream()
                .collect(Collectors.toMap(
                        row -> LocalDate.parse(row[0].toString()),
                        row -> ((Number) row[1]).longValue()
                ));

        // 生成近30天完整数据，无记录则补0
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<TakeOutDailyStatisticsVO> result = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDate date = startDate.plusDays(i);
            result.add(TakeOutDailyStatisticsVO.builder()
                    .date(date.format(dateFormatter))
                    .count(countMap.getOrDefault(date, 0L))
                    .build());
        }

        log.info("查询近30天取出统计成功，用户ID：{}，数据条数：{}", currentUserId, result.size());
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * 查询当前用户近30天每日添加物品次数，按日期升序返回，无数据日期补0。
     */
    @Override
    public List<TakeOutDailyStatisticsVO> getRecent30DaysAddStatistics(Long fridgeId) {
        Long currentUserId = getCurrentUserId();
        log.info("查询近30天添加统计，用户ID：{}，冰箱ID：{}", currentUserId, fridgeId);

        // 计算30天前的时间点（上海时区）
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
        LocalDate startDate = today.minusDays(29);
        Instant startTime = startDate.atStartOfDay(ZONE_ID_SHANGHAI).toInstant();

        // 查询数据库中有记录的日期及次数
        List<Object[]> dbResults = addRecordRepository.countDailyByOperatorIdAndTimeRange(
                currentUserId, fridgeId, startTime);

        // 将查询结果转为 Map<日期, 次数>
        Map<LocalDate, Long> countMap = dbResults.stream()
                .collect(Collectors.toMap(
                        row -> LocalDate.parse(row[0].toString()),
                        row -> ((Number) row[1]).longValue()
                ));

        // 生成近30天完整数据，无记录则补0
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<TakeOutDailyStatisticsVO> result = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDate date = startDate.plusDays(i);
            result.add(TakeOutDailyStatisticsVO.builder()
                    .date(date.format(dateFormatter))
                    .count(countMap.getOrDefault(date, 0L))
                    .build());
        }

        log.info("查询近30天添加统计成功，用户ID：{}，数据条数：{}", currentUserId, result.size());
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * 临期/过期统计逻辑，与前端 getFreshnessStatus 算法保持一致：
     * <ol>
     *   <li>保质期 &gt; 30 天的物品视为长保质期，不参与统计</li>
     *   <li>缺少 productionDate 或 shelfLifeDays 的物品跳过</li>
     *   <li>diffDays = 生产日期到今天的天数差（向下取整）</li>
     *   <li>remainingDays = shelfLifeDays - diffDays</li>
     *   <li>R = (remainingDays / shelfLifeDays) × 100</li>
     *   <li>R ≤ 0 → 已过期；0 &lt; R &lt; 20 → 临期</li>
     * </ol>
     */
    @Override
    public ExpiringSummaryVO getExpiringSummary() {
        Long currentUserId = getCurrentUserId();
        log.info("查询临期物品统计，用户ID：{}", currentUserId);

        // 获取当前用户所有冰箱ID
        List<Long> fridgeIds = fridgeRepository.findByOwnerIdAndIsDeletedFalse(currentUserId, Sort.unsorted())
                .stream()
                .map(BizFridge::getId)
                .toList();

        if (fridgeIds.isEmpty()) {
            return ExpiringSummaryVO.builder()
                    .expiringCount(0)
                    .expiredCount(0)
                    .totalExpiring(0)
                    .build();
        }

        // 查询候选物品（已过滤：未删除、有生产日期、有保质期、保质期≤30天）
        List<BizFridgeItem> candidates = itemRepository.findExpiringCandidates(fridgeIds);

        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
        int expiredCount = 0;
        int expiringCount = 0;

        for (BizFridgeItem item : candidates) {
            // 计算从生产日期到今天的天数差（与前端 Math.floor(diffTime / 86400000) 对应）
            long diffDays = ChronoUnit.DAYS.between(item.getProductionDate(), today);
            int remainingDays = item.getShelfLifeDays() - (int) diffDays;
            double r = ((double) remainingDays / item.getShelfLifeDays()) * 100.0;

            if (r <= 0) {
                expiredCount++;
            } else if (r < 20) {
                expiringCount++;
            }
            // R >= 20 的物品为"一般"或"新鲜"，不纳入统计
        }

        log.info("查询临期统计成功，用户ID：{}，临期：{}，过期：{}，总计：{}",
                currentUserId, expiringCount, expiredCount, expiringCount + expiredCount);

        return ExpiringSummaryVO.builder()
                .expiringCount(expiringCount)
                .expiredCount(expiredCount)
                .totalExpiring(expiringCount + expiredCount)
                .build();
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

        // 新鲜度排序在内存中进行，数据库层不排序
        Sort sort = "freshness".equals(request.getSortField())
                ? Sort.unsorted()
                : buildSort(request.getSortField(), request.getSortOrder());

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

        // 若按新鲜度排序，在内存中计算 r 值并排序
        if ("freshness".equals(request.getSortField())) {
            items = sortByFreshness(items, request.getSortOrder());
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
     * 按新鲜度（r 值）对物品列表进行内存排序。
     * <p>
     * r = (remainingDays / shelfLifeDays) × 100
     * 无法计算 r 值的物品（缺少生产日期/保质期、或长保质期）统一放在最后。
     *
     * @param items     物品列表
     * @param sortOrder 排序方向：asc（r 值从小到大，过期优先）、desc（r 值从大到小，新鲜优先）
     * @return 排序后的物品列表
     */
    private List<BizFridgeItem> sortByFreshness(List<BizFridgeItem> items, String sortOrder) {
        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);

        return items.stream()
                .sorted((a, b) -> {
                    Double rA = calculateFreshnessScore(a, today);
                    Double rB = calculateFreshnessScore(b, today);

                    boolean validA = rA != null;
                    boolean validB = rB != null;

                    // 无法计算 r 值的统一放最后
                    if (!validA && !validB) {
                        return 0;
                    }
                    if (!validA) {
                        return 1;
                    }
                    if (!validB) {
                        return -1;
                    }

                    int comparison = Double.compare(rA, rB);
                    return "asc".equalsIgnoreCase(sortOrder) ? comparison : -comparison;
                })
                .collect(Collectors.toList());
    }

    /**
     * 计算物品新鲜度分数（r 值）。
     *
     * @param item  物品实体
     * @param today 当前日期
     * @return r 值；若无法计算则返回 null（长保质期、缺少生产日期或保质期）
     */
    private Double calculateFreshnessScore(BizFridgeItem item, LocalDate today) {
        if (item.getProductionDate() == null || item.getShelfLifeDays() == null) {
            return null;
        }
        if (item.getShelfLifeDays() > 30) {
            return null; // 长保质期不参与 r 值排序
        }
        long diffDays = ChronoUnit.DAYS.between(item.getProductionDate(), today);
        int remainingDays = item.getShelfLifeDays() - (int) diffDays;
        return ((double) remainingDays / item.getShelfLifeDays()) * 100.0;
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

        // 批量查询单位信息（过滤已删除）
        Set<Long> unitIds = items.stream()
                .map(BizFridgeItem::getItemUnitId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, BizItemUnit> unitMap = unitRepository.findAllById(unitIds).stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .collect(Collectors.toMap(BizItemUnit::getId, Function.identity()));

        // 批量查询单位类型名称（过滤已删除）
        Set<Long> unitTypeIds = unitMap.values().stream()
                .map(BizItemUnit::getUnitTypeId)
                .collect(Collectors.toSet());
        Map<Long, String> unitTypeMap = unitTypeRepository.findAllById(unitTypeIds).stream()
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
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
        return ItemVO.builder()
                .id(item.getId())
                .fridgeId(item.getFridgeId())
                .itemName(item.getItemName())
                .itemUnitId(item.getItemUnitId())
                .unitName(resolveUnitName(item.getItemUnitId(), unitMap))
                .unitTypeId(resolveUnitTypeId(item.getItemUnitId(), unitMap))
                .unitTypeName(resolveUnitTypeName(item.getItemUnitId(), unitMap, unitTypeMap))
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
     * 解析物品的单位名称。
     * <p>
     * 如果单位 ID 为空，则返回 null；
     * 如果单位已被软删除（在 unitMap 中不存在），则返回"未知"；
     * 否则返回单位原始名称。
     *
     * @param itemUnitId 单位ID
     * @param unitMap    单位ID到单位实体的映射（仅包含未删除的单位）
     * @return 单位名称
     */
    private String resolveUnitName(Long itemUnitId, Map<Long, BizItemUnit> unitMap) {
        if (itemUnitId == null) {
            return null;
        }
        BizItemUnit unit = unitMap.get(itemUnitId);
        return unit != null ? unit.getUnitName() : "未知";
    }

    /**
     * 解析物品的单位类型ID。
     * <p>
     * 如果单位 ID 为空或单位已被软删除，则返回 null；
     * 否则返回单位关联的类型ID。
     *
     * @param itemUnitId 单位ID
     * @param unitMap    单位ID到单位实体的映射（仅包含未删除的单位）
     * @return 单位类型ID
     */
    private Long resolveUnitTypeId(Long itemUnitId, Map<Long, BizItemUnit> unitMap) {
        if (itemUnitId == null) {
            return null;
        }
        BizItemUnit unit = unitMap.get(itemUnitId);
        return unit != null ? unit.getUnitTypeId() : null;
    }

    /**
     * 解析物品的单位类型名称。
     * <p>
     * 如果单位 ID 为空或单位已被软删除，则返回 null；
     * 如果单位类型已被软删除（在 unitTypeMap 中不存在），则返回"未知"；
     * 否则返回单位类型原始名称。
     *
     * @param itemUnitId  单位ID
     * @param unitMap     单位ID到单位实体的映射（仅包含未删除的单位）
     * @param unitTypeMap 单位类型ID到名称的映射（仅包含未删除的类型）
     * @return 单位类型名称
     */
    private String resolveUnitTypeName(Long itemUnitId, Map<Long, BizItemUnit> unitMap, Map<Long, String> unitTypeMap) {
        if (itemUnitId == null) {
            return null;
        }
        BizItemUnit unit = unitMap.get(itemUnitId);
        if (unit == null) {
            return "未知";
        }
        String typeName = unitTypeMap.get(unit.getUnitTypeId());
        return typeName != null ? typeName : "未知";
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
