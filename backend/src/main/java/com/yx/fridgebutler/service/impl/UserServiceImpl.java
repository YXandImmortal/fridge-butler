package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.user.BindEmailRequest;
import com.yx.fridgebutler.dto.user.UserChangePasswordRequest;
import com.yx.fridgebutler.dto.user.UserEmailCaptchaRequest;
import com.yx.fridgebutler.dto.user.UserInitPasswordRequest;
import com.yx.fridgebutler.dto.user.UserUpdateAvatarRequest;
import com.yx.fridgebutler.dto.user.UserUpdateRequest;
import com.yx.fridgebutler.enums.EmailTemplate;
import com.yx.fridgebutler.service.EmailService;
import com.yx.fridgebutler.service.NotificationService;
import com.yx.fridgebutler.vo.user.UserInfoVO;
import com.yx.fridgebutler.entity.SysRole;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.SysRoleRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.UserService;
import com.yx.fridgebutler.util.CaptchaManager;
import com.yx.fridgebutler.util.EmailCaptchaManager;
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

    @Autowired
    private EmailCaptchaManager emailCaptchaManager;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    /** 绑定邮箱业务类型标识。 */
    private static final String TYPE_BIND = "BIND";

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
                .email(user.getEmail())
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
            log.warn("修改密码失败，验证码错误：{}，验证码ID：{}", request.getCaptcha(), request.getCaptchaId());
            throw BusinessException.loginCaptchaError();
        }

        String username = getUsernameFromToken();

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            log.warn("修改密码失败，用户名：{}两次密码不一致", username);
            throw BusinessException.changePasswordNotMatch();
        }

        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);

        if (!passwordEncoder.matches(request.getOriginalPassword(), user.getPassword())) {
            log.warn("修改密码失败，用户名：{}，原密码错误", username);
            throw BusinessException.changePasswordOriginalWrong();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordUpdatedAt(Instant.now());
        userRepository.save(user);

        // 发送密码修改成功通知
        notificationService.createSystemNotification(
                user.getId(),
                "安全提醒：密码已修改",
                "您的账号密码已被修改。如非本人操作，请立即联系管理员。",
                "NONE"
        );

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
            log.warn("初始化密码失败，用户名：{}两次密码不一致", username);
            throw BusinessException.changePasswordNotMatch();
        }

        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);

        // 仅允许从未修改过密码的用户调用（passwordUpdatedAt 为 null）
        if (user.getPasswordUpdatedAt() != null) {
            log.warn("初始化密码失败，用户名：{}已设置过密码", username);
            throw BusinessException.initPasswordNotAllowed();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordUpdatedAt(Instant.now());
        userRepository.save(user);

        log.info("首次登录密码初始化成功，用户名：{}", username);
    }

    /**
     * {@inheritDoc}
     * <p>
     * 发送绑定/修改邮箱验证码流程：
     * <ol>
     *   <li>校验目标邮箱是否已被其他用户绑定</li>
     *   <li>生成 6 位数字验证码（含 60 秒发送频率限制）</li>
     *   <li>异步发送验证码邮件</li>
     * </ol>
     */
    @Override
    public void sendBindEmailCaptcha(UserEmailCaptchaRequest request) {
        String email = normalizeEmail(request.getEmail());
        log.info("发送绑定邮箱验证码请求，邮箱：{}", email);

        String currentUsername = getUsernameFromToken();
        SysUser currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(BusinessException::userNotFound);

        // 检查邮件服务是否启用
        if (!emailService.isEmailEnabled()) {
            log.error("邮件服务未启用，无法发送绑定邮箱验证码到：{}", email);
            throw BusinessException.emailSendFailed();
        }

        // 校验邮箱是否已被其他用户绑定
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(currentUser.getId())) {
                log.warn("发送绑定邮箱验证码失败，邮箱已被其他用户绑定：{}", email);
                throw BusinessException.emailAlreadyBound();
            }
        });

        try {
            String captcha = emailCaptchaManager.generateCaptcha(TYPE_BIND, email);
            emailService.sendTemplateMailSync(email, EmailTemplate.EMAIL_VERIFICATION, captcha);
            log.info("绑定邮箱验证码已生成并发送，当前用户：{}，邮箱：{}", currentUsername, email);
        } catch (IllegalStateException e) {
            log.warn("绑定邮箱验证码发送过于频繁，邮箱：{}", email);
            throw BusinessException.emailSendTooFrequent();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * 绑定/修改邮箱流程：
     * <ol>
     *   <li>校验当前登录用户</li>
     *   <li>校验邮箱验证码是否正确</li>
     *   <li>校验邮箱是否已被其他用户绑定</li>
     *   <li>更新用户邮箱</li>
     * </ol>
     */
    @Override
    public void bindEmail(BindEmailRequest request) {
        String email = normalizeEmail(request.getEmail());
        log.info("绑定邮箱请求，邮箱：{}", email);

        String currentUsername = getUsernameFromToken();
        SysUser currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(BusinessException::userNotFound);

        // 校验邮箱验证码
        if (!emailCaptchaManager.verifyCaptcha(TYPE_BIND, email, request.getCaptcha())) {
            log.warn("绑定邮箱失败，验证码错误，用户：{}，邮箱：{}", currentUsername, email);
            throw BusinessException.emailCaptchaError();
        }

        // 校验邮箱是否已被其他用户绑定
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(currentUser.getId())) {
                log.warn("绑定邮箱失败，邮箱已被其他用户绑定：{}", email);
                throw BusinessException.emailAlreadyBound();
            }
        });

        // 如果邮箱没有变化，直接返回成功
        if (email.equals(normalizeEmail(currentUser.getEmail()))) {
            log.info("绑定邮箱成功，邮箱未发生变化，用户：{}，邮箱：{}", currentUsername, email);
            return;
        }

        // 更新邮箱
        currentUser.setEmail(email);
        currentUser.setUpdateTime(Instant.now());
        userRepository.save(currentUser);

        // 清除绑定邮箱提醒通知
        notificationService.clearBindEmailReminder(currentUser.getId());

        // 发送邮箱绑定成功通知
        notificationService.createSystemNotification(
                currentUser.getId(),
                "邮箱绑定成功",
                "您的账号已绑定邮箱 " + email + "，可用于密码找回和接收安全通知。",
                "NONE"
        );

        log.info("绑定邮箱成功，用户：{}，邮箱：{}", currentUsername, email);
    }

    /**
     * 标准化邮箱地址
     *
     * @param email 原始邮箱
     * @return 去除首尾空格并转小写后的邮箱
     */
    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
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
