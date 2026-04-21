package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.FridgeCreateRequest;
import com.yx.fridgebutler.dto.FridgeQueryRequest;
import com.yx.fridgebutler.dto.FridgeDTO;
import com.yx.fridgebutler.entity.BizFridge;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.BizFridgeRepository;
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
import java.util.List;

@Slf4j
@Service
public class FridgeServiceImpl implements FridgeService {

    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private BizFridgeRepository fridgeRepository;

    @Autowired
    private SysUserRepository userRepository;

    @Override
    public List<FridgeDTO> listMyFridges(FridgeQueryRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("查询用户冰箱列表，用户ID：{}，排序字段：{}，排序方向：{}",
                currentUserId, request.getSortField(), request.getSortOrder());

        Sort sort = buildSort(request.getSortField(), request.getSortOrder());
        List<BizFridge> fridges = fridgeRepository.findByOwnerIdAndIsDeletedFalse(currentUserId, sort);

        return fridges.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public FridgeDTO getFridgeDetail(Long id) {
        Long currentUserId = getCurrentUserId();
        log.info("查询冰箱详情，冰箱ID：{}，用户ID：{}", id, currentUserId);

        BizFridge fridge = fridgeRepository.findByIdAndOwnerIdAndIsDeletedFalse(id, currentUserId)
                .orElseThrow(BusinessException::notFound);

        return convertToDTO(fridge);
    }

    @Override
    @Transactional
    public Long createFridge(FridgeCreateRequest request) {
        Long currentUserId = getCurrentUserId();
        log.info("创建冰箱，用户ID：{}，冰箱名称：{}", currentUserId, request.getFridgeName());

        if (fridgeRepository.existsByFridgeNameAndOwnerIdAndIsDeletedFalse(request.getFridgeName(), currentUserId)) {
            log.error("创建冰箱失败，冰箱名称已存在：{}，用户ID：{}", request.getFridgeName(), currentUserId);
            throw new BusinessException(400, "冰箱名称已存在");
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            fridgeRepository.unsetDefaultByOwnerId(currentUserId);
        }

        BizFridge fridge = new BizFridge();
        fridge.setFridgeName(request.getFridgeName());
        fridge.setFridgeAddress(request.getFridgeAddress());
        fridge.setTotalCapacity(request.getTotalCapacity());
        fridge.setIsDefault(request.getIsDefault());
        fridge.setOwnerId(currentUserId);
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

    private static Sort buildSort(String sortField, String sortOrder) {
        if (sortField == null) sortField = "createTime";

        String field = switch (sortField) {
            case "name" -> "fridgeName";
            case "totalCapacity" -> "totalCapacity";
            default -> throw new BusinessException(400,
                    "未知的排序字段：" + sortField
            );
        };

        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }

    private FridgeDTO convertToDTO(BizFridge fridge) {
        return FridgeDTO.builder()
                .id(fridge.getId())
                .fridgeName(fridge.getFridgeName())
                .isDefault(fridge.getIsDefault())
                .fridgeAddress(fridge.getFridgeAddress())
                .totalCapacity(fridge.getTotalCapacity())
                .createTime(formatInstant(fridge.getCreateTime()))
                .updateTime(formatInstant(fridge.getUpdateTime()))
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
