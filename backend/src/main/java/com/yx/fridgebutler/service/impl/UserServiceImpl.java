package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.user.UserChangePasswordRequest;
import com.yx.fridgebutler.dto.user.UserInitPasswordRequest;
import com.yx.fridgebutler.dto.user.UserUpdateAvatarRequest;
import com.yx.fridgebutler.dto.user.UserUpdateRequest;
import com.yx.fridgebutler.vo.UserInfoVO;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 用户服务实现类。
 * <p>处理用户信息查询、更新、密码修改、头像更新等业务逻辑。</p>
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    /**
     * 上海时区，用于时间格式化。
     */
    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");

    /**
     * 日期时间格式化器，格式为 yyyy-MM-dd HH:mm:ss。
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private SysRoleRepository roleRepository;

    @Autowired
    private CaptchaManager captchaManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     * <p>从 Security 上下文中获取当前用户名，查询用户及角色信息后组装为 VO 返回。</p>
     */
    @Override
    public UserInfoVO getUserInfo() {
        String username = getUsernameFromToken();

        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);

        SysRole role = roleRepository.findById(user.getRoleId())
                .orElseThrow(BusinessException::roleNotFound);

        return UserInfoVO.builder()
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .mobile(user.getMobile())
                .roleName(role.getRoleName())
                .createTime(user.getCreateTime()
                        .atZone(ZONE_ID_SHANGHAI)
                        .format(DATE_TIME_FORMATTER))
                .guideCompleted(user.getGuideCompleted())
                .isActivated(user.getIsActivated())
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>更新前会校验用户名和手机号是否被其他用户占用。</p>
     */
    @Override
    public void updateUser(UserUpdateRequest request) {
        String currentUsername = getUsernameFromToken();

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
        // 将空白手机号统一转为 null，避免空字符串触发唯一键冲突
        if (newMobile != null && newMobile.isBlank()) {
            newMobile = null;
        }
        if (newMobile != null && !newMobile.equals(user.getMobile())) {
            userRepository.findByUsernameOrMobile(newMobile, newMobile).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(user.getId())) {
                    throw BusinessException.updateUserPhoneExist();
                }
            });
        }

        user.setUsername(newUsername);
        user.setMobile(newMobile);

        userRepository.save(user);
        log.info("用户信息更新成功，用户名：{}，手机号：{}", newUsername, request.getMobile());
    }

    /**
     * {@inheritDoc}
     * <p>修改前会校验验证码、新密码一致性以及原密码正确性。</p>
     */
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

        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);

        if (!passwordEncoder.matches(request.getOriginalPassword(), user.getPassword())) {
            log.error("修改密码失败，用户名：{}，原密码错误", username);
            throw BusinessException.changePasswordOriginalWrong();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordUpdatedAt(Instant.now());
        userRepository.save(user);

        log.info("密码修改成功，用户名：{}", username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAvatar(UserUpdateAvatarRequest request) {
        String username = getUsernameFromToken();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);
        user.setAvatar(request.getAvatar());
        userRepository.save(user);
        log.info("头像修改成功，用户名：{}，新头像Id：{}", username, request.getAvatar());
    }

    /**
     * {@inheritDoc}
     * <p>将当前登录用户的 guide_completed 字段设为 true。</p>
     */
    @Override
    public void completeGuide() {
        String username = getUsernameFromToken();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);
        user.setGuideCompleted(true);
        userRepository.save(user);
        log.info("新手指引标记完成，用户名：{}", username);
    }

    /**
     * {@inheritDoc}
     * <p>首次登录初始化密码，无需原密码和验证码。</p>
     */
    @Override
    public void initPassword(UserInitPasswordRequest request) {
        String username = getUsernameFromToken();

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            log.error("初始化密码失败，用户名：{}两次密码不一致", username);
            throw BusinessException.changePasswordNotMatch();
        }

        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);

        // 仅允许从未修改过密码的用户调用（passwordUpdatedAt 为 null）
        if (user.getPasswordUpdatedAt() != null) {
            log.error("初始化密码失败，用户名：{}已设置过密码", username);
            throw BusinessException.initPasswordNotAllowed();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordUpdatedAt(Instant.now());
        userRepository.save(user);

        log.info("首次登录密码初始化成功，用户名：{}", username);
    }

    /**
     * 从 Spring Security 上下文中获取当前登录用户名。
     *
     * @return 当前用户名
     * @throws BusinessException 如果未获取到认证信息则抛出异常
     */
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
