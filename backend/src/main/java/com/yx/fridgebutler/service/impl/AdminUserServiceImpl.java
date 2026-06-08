package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.admin.AdminUserQueryRequest;
import com.yx.fridgebutler.entity.SysRole;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.SysRoleRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.AdminUserService;
import com.yx.fridgebutler.service.NotificationService;
import com.yx.fridgebutler.vo.admin.AdminUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 管理员用户管理服务实现类
 */
@Slf4j
@Service
public class AdminUserServiceImpl implements AdminUserService {

    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
    private static final int PASSWORD_LENGTH = 12;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private SysRoleRepository sysRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationService notificationService;

    /**
     * {@inheritDoc}
     * <p>分页查询用户列表，支持关键词（用户名/手机号）和状态筛选。</p>
     */
    @Override
    public Page<AdminUserVO> getUserList(AdminUserQueryRequest request) {
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;

        // 构建排序规则
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        Sort sort;
        if ("lastLoginTime".equals(sortField)) {
            sort = "asc".equalsIgnoreCase(sortOrder)
                    ? Sort.by("lastLoginTime").ascending()
                    : Sort.by("lastLoginTime").descending();
        } else {
            // 默认按 createTime 排序
            sort = "asc".equalsIgnoreCase(sortOrder)
                    ? Sort.by("createTime").ascending()
                    : Sort.by("createTime").descending();
        }
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        // 前端 status=true 表示正常，false 表示禁用；数据库 isDeleted 与之相反
        Boolean dbStatus = request.getStatus() != null ? !request.getStatus() : null;
        Page<SysUser> userPage = sysUserRepository.findByKeywordAndStatus(
                request.getKeyword(), dbStatus, pageable);

        return userPage.map(this::convertToVO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AdminUserVO getUserDetail(Long id) {
        SysUser user = sysUserRepository.findById(id)
                .orElseThrow(BusinessException::userNotFound);
        return convertToVO(user);
    }

    /**
     * {@inheritDoc}
     * <p>禁止管理员禁用自己。</p>
     */
    @Override
    public void updateUserStatus(Long id, Boolean status) {
        SysUser user = sysUserRepository.findById(id)
                .orElseThrow(BusinessException::userNotFound);

        // 禁止禁用当前登录账号（前端 status=false 表示禁用）
        String currentUsername = getCurrentUsername();
        if (user.getUsername().equals(currentUsername) && Boolean.FALSE.equals(status)) {
            log.warn("管理员尝试禁用自己，操作被拒绝，用户ID：{}", id);
            throw BusinessException.adminCannotDisableSelf();
        }

        // 前端 status=true 表示正常，false 表示禁用；数据库 isDeleted 与之相反
        user.setIsDeleted(!status);
        sysUserRepository.save(user);
        log.info("管理员更新用户状态成功，用户ID：{}，用户名：{}，新状态：{}", id, user.getUsername(), status);
    }

    /**
     * {@inheritDoc}
     * <p>生成随机强密码，BCrypt 加密后更新到数据库。</p>
     */
    @Override
    public String resetUserPassword(Long id) {
        SysUser user = sysUserRepository.findById(id)
                .orElseThrow(BusinessException::userNotFound);

        String rawPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPasswordUpdatedAt(null);
        sysUserRepository.save(user);

        // 发送管理员重置密码通知
        notificationService.createSystemNotification(
                user.getId(),
                "系统通知：密码已被重置",
                "管理员已重置您的账号密码，请使用新密码登录，建议登录后尽快修改。",
                "NONE"
        );

        log.info("管理员重置用户密码成功，用户ID：{}，用户名：{}", id, user.getUsername());
        return rawPassword;
    }

    /**
     * 将 SysUser 实体转换为 AdminUserVO
     *
     * @param user 用户实体
     * @return 用户管理 VO
     */
    private AdminUserVO convertToVO(SysUser user) {
        SysRole role = sysRoleRepository.findById(user.getRoleId()).orElse(null);

        return AdminUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .mobile(user.getMobile())
                .avatar(user.getAvatar())
                .roleId(user.getRoleId())
                .roleName(role != null ? role.getRoleName() : null)
                .status(!user.getIsDeleted())
                .createTime(user.getCreateTime() != null
                        ? user.getCreateTime().atZone(ZONE_ID_SHANGHAI).format(DATE_TIME_FORMATTER)
                        : null)
                .lastLoginTime(user.getLastLoginTime() != null
                        ? user.getLastLoginTime().atZone(ZONE_ID_SHANGHAI).format(DATE_TIME_FORMATTER)
                        : null)
                .updateTime(user.getUpdateTime() != null
                        ? user.getUpdateTime().atZone(ZONE_ID_SHANGHAI).format(DATE_TIME_FORMATTER)
                        : null)
                .build();
    }

    /**
     * 获取当前登录用户名
     *
     * @return 当前用户名
     */
    private String getCurrentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    /**
     * 生成随机密码
     *
     * @return 随机密码字符串
     */
    private String generateRandomPassword() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
