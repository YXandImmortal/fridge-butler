package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.UserChangePasswordRequest;
import com.yx.fridgebutler.dto.UserUpdateAvatarRequest;
import com.yx.fridgebutler.dto.UserUpdateRequest;
import com.yx.fridgebutler.dto.UserInfoDTO;
import com.yx.fridgebutler.entity.SysRole;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.SysRoleRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.UserService;
import com.yx.fridgebutler.util.CaptchaManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private CaptchaManager captchaManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserInfoDTO getUserInfo() {
        // 从SecurityContextHolder中获取当前用户名
        String username = getUsernameFromToken();

        // 查询用户信息
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);

        // 查询角色信息
        SysRole role = roleRepository.findById(user.getRoleId())
                .orElseThrow(BusinessException::roleNotFound);

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
    public void updateUser(UserUpdateRequest request) {
        // 从SecurityContextHolder中获取当前用户名
        String currentUsername = getUsernameFromToken();

        // 查询用户信息
        SysUser user = userRepository.findByUsername(currentUsername)
                .orElseThrow(BusinessException::userNotFound);

        // 如果用户名发生变化，校验新用户名是否已被其他用户占用
        String newUsername = request.getUsername();
        if (!currentUsername.equals(newUsername)) {
            userRepository.findByUsername(newUsername).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(user.getId())) {
                    throw BusinessException.updateUserUsernameExist();
                }
            });
        }

        // 如果手机号发生变化，校验新手机号是否已被其他用户占用
        String newMobile = request.getMobile();
        if (newMobile != null && !newMobile.equals(user.getMobile())) {
            userRepository.findByUsernameOrMobile(newMobile, newMobile).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(user.getId())) {
                    throw BusinessException.updateUserPhoneExist();
                }
            });
        }

        // 更新用户信息
        user.setUsername(newUsername);
        user.setMobile(newMobile);

        // 保存更新
        userRepository.save(user);
        log.info("用户信息更新成功，用户名：{}，手机号：{}", newUsername, request.getMobile());
    }

    @Override
    public void changePassword(UserChangePasswordRequest request) {
        if (request.getCaptchaId() == null || !captchaManager.verifyCaptcha(request.getCaptchaId(), request.getCaptcha())) {
            log.error("修改密码失败，验证码错误：{}，验证码ID：{}", request.getCaptcha(), request.getCaptchaId());
            throw BusinessException.loginCaptchaError();
        }

        String username = getUsernameFromToken();

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            log.error("修改密码失败，用户名：{}两次密码不一致", username);
            throw BusinessException.changePasswordNotMatch();
        }

        SysUser user =  userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);

        if (!passwordEncoder.matches(request.getOriginalPassword(), user.getPassword())) {
            log.error("修改密码失败，用户名：{}，原密码错误", username);
            throw BusinessException.changePasswordOriginalWrong();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("密码修改成功，用户名：{}，新密码：{}", username, request.getNewPassword());
    }

    @Override
    public void updateAvatar(UserUpdateAvatarRequest request) {
        String username = getUsernameFromToken();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);
        user.setAvatar(request.getAvatar());
        userRepository.save(user);
        log.info("头像修改成功，用户名：{}，新头像Id：{}", username, request.getAvatar());
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