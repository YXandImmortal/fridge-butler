package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.ChangePasswordRequest;
import com.yx.fridgebutler.dto.UpdateAvatarRequest;
import com.yx.fridgebutler.dto.UpdateRequest;
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

    @Override
    public void changePassword(ChangePasswordRequest request) {
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
                .orElseThrow(BusinessException::notFound);

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("密码修改成功，用户名：{}，新密码：{}", username, request.getNewPassword());
    }

    @Override
    public void updateAvatar(UpdateAvatarRequest request) {
        String username = getUsernameFromToken();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::notFound);
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