package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.UpdateRequest;
import com.yx.fridgebutler.dto.UserInfoDTO;
import com.yx.fridgebutler.entity.SysRole;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.SysRoleRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    // 日期时间格式化常量
    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private SysRoleRepository roleRepository;

    @Override
    public UserInfoDTO getUserInfo() {
        // 从SecurityContextHolder中获取当前用户名
        String username = getUsernameFromToken();

        // 查询用户信息
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::notFound);

        // 查询角色信息
        SysRole role = roleRepository.findById(user.getRoleId())
                .orElseThrow(BusinessException::notFound);

        // 构建UserInfoDTO
        return UserInfoDTO.builder()
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .mobile(user.getMobile())
                .roleName(role.getRoleName())
                .createTime(user.getCreateTime()
                        .atZone(ZONE_ID_SHANGHAI)
                        .format(DATE_TIME_FORMATTER))
                .build();
    }

    @Override
    public void updateUser(UpdateRequest request) {
        // 从SecurityContextHolder中获取当前用户名
        String currentUsername = getUsernameFromToken();

        // 查询用户信息
        SysUser user = userRepository.findByUsername(currentUsername)
                .orElseThrow(BusinessException::notFound);

        // 更新用户信息
        user.setUsername(request.getUsername());
        user.setMobile(request.getMobile());

        // 保存更新
        userRepository.save(user);
        log.info("用户信息更新成功，用户名：{}，手机号：{}", request.getUsername(), request.getMobile());
    }

    private static String getUsernameFromToken() {
        String username = null;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            username = authentication.getName();
        }
        if (username == null) {
            throw BusinessException.notFound();
        }
        log.info("获取用户信息，用户名：{}", username);
        return username;
    }
}