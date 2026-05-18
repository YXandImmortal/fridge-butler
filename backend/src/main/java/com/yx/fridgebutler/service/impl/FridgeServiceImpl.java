package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.FridgeCreateRequest;
import com.yx.fridgebutler.vo.FridgeVO;
import com.yx.fridgebutler.dto.FridgeSearchRequest;
import com.yx.fridgebutler.dto.FridgeUpdateRequest;
import com.yx.fridgebutler.entity.BizFridge;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.BizFridgeRepository;
import com.yx.fridgebutler.repository.BizFridgeItemRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.FridgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * 冰箱服务实现类，处理冰箱的查询、创建、更新、删除以及默认冰箱管理等核心业务逻辑。
 * <p>
 * 所有操作均基于当前登录用户进行权限校验，确保用户只能访问和管理自己的冰箱数据。
 */
@Slf4j
@Service
public class FridgeServiceImpl implements FridgeService {

    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private BizFridgeItemRepository itemRepository;

    /**
     * {@inheritDoc}
     * <p>
     * 查询结果按默认状态优先排序（默认冰箱排在最前面），同状态下按创建时间升序排列。
     */
    @Override
    public List<FridgeVO> listMyFridges() {
        Long currentUserId = getCurrentUserId();
        log.info("查询用户冰箱列表，用户ID：{}，排序字段：createTime，排序方向：asc",
                currentUserId);

        Sort sort = buildSort("createTime", "asc");
        List<BizFridge> fridges = fridgeRepository.findByOwnerIdAndIsDeletedFalse(currentUserId, sort);

        // 默认冰箱排在前面
        return fridges.stream()
                .map(this::convertToVO)
                .sorted(Comparator.comparing(FridgeVO::getIsDefault, Comparator.reverseOrder()))
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FridgeVO getFridgeDetail(Long id) {
        Long currentUserId = getCurrentUserId();
        log.info("查询冰箱详情，冰箱ID：{}，用户ID：{}", id, currentUserId);

        BizFridge fridge = fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(id, currentUserId)
                .orElseThrow(BusinessException::fridgeNotFound);

        return convertToVO(fridge);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 创建前会校验冰箱名称在当前用户下是否已存在，避免重复。
     */
    @Override
    @Transactional
    public Long createFridge(FridgeCreateRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("创建冰箱，用户ID：{}，冰箱名称：{}", currentUserId, request.getFridgeName());

        // 校验冰箱名称是否已存在
        if (fridgeRepository.existsByFridgeNameAndOwnerIdAndIsDeletedFalse(request.getFridgeName(), currentUserId)) {
            log.error("创建冰箱失败，冰箱名称已存在：{}，用户ID：{}", request.getFridgeName(), currentUserId);
            throw BusinessException.fridgeNameExists();
        }

        BizFridge fridge = new BizFridge();
        fridge.setFridgeName(request.getFridgeName());
        fridge.setFridgeAddress(request.getFridgeAddress());
        fridge.setRemark(request.getRemark());
        fridge.setOwnerId(currentUserId);
        fridge.setIsDefault(false);
        fridge.setStatus(true);
        fridge.setIsDeleted(false);

        Instant now = Instant.now();
        fridge.setCreateTime(now);
        fridge.setUpdateTime(now);

        BizFridge saved = fridgeRepository.save(fridge);
        log.info("冰箱创建成功，冰箱ID：{}，名称：{}", saved.getId(), saved.getFridgeName());
        return saved.getId();
    }

    @Override
    @Transactional
    public void updateFridge(Long id, FridgeUpdateRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("更新冰箱，冰箱ID：{}，用户ID：{}", id, currentUserId);

        BizFridge fridge = fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(id, currentUserId)
                .orElseThrow(BusinessException::notFound);

        // 校验名称是否与其他冰箱重复
        if (!fridge.getFridgeName().equals(request.getFridgeName())
                && fridgeRepository.existsByFridgeNameAndOwnerIdAndIsDeletedFalse(request.getFridgeName(), currentUserId)) {
            log.error("更新冰箱失败，冰箱名称已存在：{}，用户ID：{}", request.getFridgeName(), currentUserId);
            throw BusinessException.updateFridgeNameExists();
        }

        // 如果设置为默认冰箱，取消其他默认冰箱
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(fridge.getIsDefault())) {
            fridgeRepository.unsetDefaultByOwnerId(currentUserId);
        }

        fridge.setFridgeName(request.getFridgeName());
        fridge.setFridgeAddress(request.getFridgeAddress());
        fridge.setRemark(request.getRemark());
        fridge.setTotalCapacity(request.getTotalCapacity());
        fridge.setIsDefault(request.getIsDefault());
        if (request.getStatus() != null) {
            fridge.setStatus(request.getStatus());
        }
        fridge.setUpdateTime(Instant.now());

        fridgeRepository.save(fridge);
        log.info("冰箱更新成功，冰箱ID：{}", id);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 采用软删除方式，将冰箱标记为已删除状态。
     */
    @Override
    @Transactional
    public void deleteFridge(Long id) {
        Long currentUserId = getCurrentUserId();
        log.info("删除冰箱，冰箱ID：{}，用户ID：{}", id, currentUserId);

        BizFridge fridge = fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(id, currentUserId)
                .orElseThrow(BusinessException::notFound);

        fridge.setIsDeleted(true);
        fridge.setUpdateTime(Instant.now());
        fridgeRepository.save(fridge);

        log.info("冰箱删除成功，冰箱ID：{}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FridgeVO getDefaultFridge() {
        Long currentUserId = getCurrentUserId();
        log.info("查询用户默认冰箱，用户ID：{}", currentUserId);

        return fridgeRepository.findByOwnerIdAndIsDefaultTrueAndIsDeletedFalse(currentUserId)
                .map(this::convertToVO)
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 支持按关键词模糊搜索冰箱名称，结果按默认状态优先排序。
     */
    @Override
    public List<FridgeVO> searchFridges(FridgeSearchRequest request) {
        Long currentUserId = getCurrentUserId();
        String keyword = request.getKeyword();
        log.info("搜索冰箱，用户ID：{}，关键词：{}", currentUserId, keyword);

        String likeKeyword = (keyword == null || keyword.isBlank()) ? "" : "%" + keyword + "%";
        Sort sort = buildSort(request.getSortField(), request.getSortOrder());

        List<BizFridge> fridges = fridgeRepository.searchByKeyword(currentUserId, likeKeyword, sort);

        // 默认冰箱排在前面
        return fridges.stream()
                .map(this::convertToVO)
                .sorted(Comparator.comparing(FridgeVO::getIsDefault, Comparator.reverseOrder()))
                .toList();
    }

    /**
     * 构建排序对象。
     *
     * @param sortField 排序字段（name/totalCapacity/createTime）
     * @param sortOrder 排序方向（asc/desc）
     * @return Spring Data的Sort对象
     */
    private static Sort buildSort(String sortField, String sortOrder) {
        if (sortField == null) sortField = "createTime";

        String field = switch (sortField) {
            case "name" -> "fridgeName";
            case "totalCapacity" -> "totalCapacity";
            case "createTime" -> "createTime";
            default -> throw BusinessException.unknownSortField();
        };

        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }

    /**
     * 将冰箱实体转换为视图对象。
     *
     * @param fridge 冰箱实体
     * @return 冰箱视图对象
     */
    private FridgeVO convertToVO(BizFridge fridge) {
        return FridgeVO.builder()
                .id(fridge.getId())
                .fridgeName(fridge.getFridgeName())
                .isDefault(fridge.getIsDefault())
                .remark(fridge.getRemark())
                .fridgeAddress(fridge.getFridgeAddress())
                .status(fridge.getStatus())
                .totalCapacity(fridge.getTotalCapacity())
                .itemCount((int) itemRepository.countByFridgeIdAndIsDeletedFalse(fridge.getId()))
                .createTime(formatInstant(fridge.getCreateTime()))
                .updateTime(formatInstant(fridge.getUpdateTime()))
                .build();
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
