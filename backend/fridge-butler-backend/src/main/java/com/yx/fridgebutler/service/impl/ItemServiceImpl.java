package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.ItemCategoryDTO;
import com.yx.fridgebutler.dto.ItemCreateRequest;
import com.yx.fridgebutler.dto.ItemDTO;
import com.yx.fridgebutler.dto.ItemSearchRequest;
import com.yx.fridgebutler.dto.ItemUnitDTO;
import com.yx.fridgebutler.dto.UnitTypeDTO;
import com.yx.fridgebutler.entity.BizFridge;
import com.yx.fridgebutler.entity.BizFridgeItem;
import com.yx.fridgebutler.entity.BizItemCategory;
import com.yx.fridgebutler.entity.BizItemUnit;
import com.yx.fridgebutler.entity.BizUnitType;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.enums.ResultCode;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.BizFridgeItemRepository;
import com.yx.fridgebutler.repository.BizFridgeRepository;
import com.yx.fridgebutler.repository.BizItemCategoryRepository;
import com.yx.fridgebutler.repository.BizItemUnitRepository;
import com.yx.fridgebutler.repository.BizUnitTypeRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    public List<ItemCategoryDTO> listItemCategories() {
        Long currentUserId = getCurrentUserId();
        log.info("查询物品分类列表，用户ID：{}", currentUserId);

        List<BizItemCategory> categories = categoryRepository.findAllByOwnerIdOrSystemDefault(currentUserId);

        return categories.stream()
                .map(c -> ItemCategoryDTO.builder()
                        .id(c.getId())
                        .categoryName(c.getCategoryName())
                        .isSystemDefault(c.getIsSystemDefault())
                        .build())
                .toList();
    }

    @Override
    public List<ItemUnitDTO> listItemUnits() {
        Long currentUserId = getCurrentUserId();
        log.info("查询物品单位列表，用户ID：{}", currentUserId);

        List<BizItemUnit> units = unitRepository.findAllByOwnerIdOrSystemDefault(currentUserId);

        Set<Long> unitTypeIds = units.stream()
                .map(BizItemUnit::getUnitTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> unitTypeMap = unitTypeRepository.findAllById(unitTypeIds).stream()
                .collect(Collectors.toMap(BizUnitType::getId, BizUnitType::getUnitTypeName));

        return units.stream()
                .map(u -> ItemUnitDTO.builder()
                        .id(u.getId())
                        .unitName(u.getUnitName())
                        .unitTypeId(u.getUnitTypeId())
                        .unitTypeName(unitTypeMap.get(u.getUnitTypeId()))
                        .isSystemDefault(u.getIsSystemDefault())
                        .build())
                .toList();
    }

    @Override
    public List<UnitTypeDTO> listUnitTypes() {
        Long currentUserId = getCurrentUserId();
        log.info("查询单位类型列表，用户ID：{}", currentUserId);

        List<BizUnitType> unitTypes = unitTypeRepository.findAllByOwnerIdOrSystemDefault(currentUserId);

        return unitTypes.stream()
                .map(t -> UnitTypeDTO.builder()
                        .id(t.getId())
                        .unitTypeName(t.getUnitTypeName())
                        .isSystemDefault(t.getIsSystemDefault())
                        .build())
                .toList();
    }

    @Override
    public Long createItem(ItemCreateRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("新增物品，用户ID：{}，冰箱ID：{}，物品名称：{}", currentUserId, request.getFridgeId(), request.getItemName());

        // 校验冰箱是否属于当前用户
        fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(request.getFridgeId(), currentUserId)
                .orElseThrow(BusinessException::notFound);

        // 校验分类是否存在（如果传了分类ID）
        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(BusinessException::notFound);
        }

        // 校验单位是否存在（如果传了单位ID）
        if (request.getItemUnitId() != null) {
            unitRepository.findById(request.getItemUnitId())
                    .orElseThrow(BusinessException::notFound);
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
        return saved.getId();
    }

    @Override
    public List<ItemDTO> searchItems(ItemSearchRequest request) {
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
                    .orElseThrow(BusinessException::notFound);
            items = itemRepository.searchItemsByFridgeId(
                    request.getFridgeId(), likeKeyword, request.getCategoryId(), request.getUnitId(), request.getUnitTypeId(), sort);
        } else {
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

        return convertToDTOList(items);
    }

    private static Sort buildSort(String sortField, String sortOrder) {
        if (sortField == null) {
            sortField = "storedDate";
        }

        String field = switch (sortField) {
            case "itemNum" -> "itemNum";
            case "storedDate" -> "storedDate";
            default -> throw new BusinessException(ResultCode.SORT_FAILED_UNKNOW_SORT_FIELD);
        };

        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }

    private List<ItemDTO> convertToDTOList(List<BizFridgeItem> items) {
        Set<Long> categoryIds = items.stream()
                .map(BizFridgeItem::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> categoryMap = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(BizItemCategory::getId, BizItemCategory::getCategoryName));

        Set<Long> unitIds = items.stream()
                .map(BizFridgeItem::getItemUnitId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, BizItemUnit> unitMap = unitRepository.findAllById(unitIds).stream()
                .collect(Collectors.toMap(BizItemUnit::getId, Function.identity()));

        Set<Long> unitTypeIds = unitMap.values().stream()
                .map(BizItemUnit::getUnitTypeId)
                .collect(Collectors.toSet());
        Map<Long, String> unitTypeMap = unitTypeRepository.findAllById(unitTypeIds).stream()
                .collect(Collectors.toMap(BizUnitType::getId, BizUnitType::getUnitTypeName));

        return items.stream()
                .map(item -> convertToDTO(item, categoryMap, unitMap, unitTypeMap))
                .toList();
    }

    private ItemDTO convertToDTO(BizFridgeItem item,
                                  Map<Long, String> categoryMap,
                                  Map<Long, BizItemUnit> unitMap,
                                  Map<Long, String> unitTypeMap) {
        BizItemUnit unit = item.getItemUnitId() != null ? unitMap.get(item.getItemUnitId()) : null;
        Long unitTypeId = unit != null ? unit.getUnitTypeId() : null;
        String unitTypeName = unitTypeId != null ? unitTypeMap.get(unitTypeId) : null;

        return ItemDTO.builder()
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
                .categoryName(categoryMap.get(item.getCategoryId()))
                .itemNum(item.getItemNum())
                .remark(item.getRemark())
                .createTime(formatInstant(item.getCreateTime()))
                .updateTime(formatInstant(item.getUpdateTime()))
                .build();
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZONE_ID_SHANGHAI).format(DATE_TIME_FORMATTER);
    }

    private Long getCurrentUserId() {
        String username = getUsernameFromToken();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::notFound);
        return user.getId();
    }

    private static String getUsernameFromToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw BusinessException.authFailed();
        }
        return authentication.getName();
    }
}
