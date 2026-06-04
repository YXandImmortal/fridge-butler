package com.yx.fridgebutler.service.impl;

import cn.hutool.core.util.PhoneUtil;
import com.yx.fridgebutler.dto.auth.LoginRequest;
import com.yx.fridgebutler.vo.LoginVO;
import com.yx.fridgebutler.dto.auth.RegisterRequest;
import com.yx.fridgebutler.entity.SysRole;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.SysConfigRepository;
import com.yx.fridgebutler.repository.SysRoleRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.AuthService;
import com.yx.fridgebutler.service.NotificationService;
import com.yx.fridgebutler.util.CaptchaManager;
import com.yx.fridgebutler.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 认证服务实现类，处理用户登录、注册等认证相关业务逻辑。
 * <p>
 * 登录流程包括：验证码校验、用户存在性校验、密码匹配校验、账号状态校验、角色存在性校验，最终生成JWT Token返回。<br>
 * 注册流程包括：验证码校验、密码一致性校验、用户名唯一性校验、手机号格式与唯一性校验，最终创建新用户。
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Long NORMAL_USER_ROLE_ID = 2L;
    private static final String DEFAULT_AVATAR_ID = "ice";
    private static final String KEY_REQUIRE_ACTIVATION = "sys.require_activation_key";

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private SysRoleRepository roleRepository;

    @Autowired
    private SysConfigRepository sysConfigRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CaptchaManager captchaManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NotificationService notificationService;

    /**
     * {@inheritDoc}
     * <p>
     * 登录流程：
     * <ol>
     *   <li>校验验证码是否正确</li>
     *   <li>根据账号（用户名或手机号）查询用户</li>
     *   <li>校验密码是否匹配</li>
     *   <li>校验账号是否被禁用</li>
     *   <li>查询用户角色信息</li>
     *   <li>检查密钥激活开关及用户激活状态</li>
     *   <li>生成JWT Token并返回登录信息</li>
     * </ol>
     */
    @Override
    public LoginVO login(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("用户登录请求，账号：{}", request.getAccount());

        // 验证验证码
        if (request.getCaptchaId() == null || !captchaManager.verifyCaptcha(request.getCaptchaId(), request.getCaptcha())) {
            log.error("登陆失败，验证码错误：{}，验证码ID：{}", request.getCaptcha(), request.getCaptchaId());
            throw BusinessException.loginCaptchaError();
        }

        // 根据用户名或手机号查询用户
        SysUser user = userRepository.findByUsernameOrMobile(
                        request.getAccount() == null ? "" : request.getAccount().trim(),
                        request.getAccount() == null ? "" : request.getAccount().trim()
                )
                .orElseThrow(() -> {
                    log.error("登陆失败，用户不存在：账号：{}", request.getAccount());
                    return BusinessException.loginAuthFailed();
                });

        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("登陆失败，账号：{}密码错误", request.getAccount());
            throw BusinessException.loginAuthFailed();
        }

        // 校验账号是否被禁用
        if (user.getIsDeleted() != null && user.getIsDeleted()) {
            log.error("登陆失败，账号：{}已被禁用", request.getAccount());
            throw BusinessException.loginForbidden();
        }

        // 查询用户角色
        SysRole role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> {
                    log.error("登陆失败，账号：{}的角色ID：{}不存在：", request.getAccount(), user.getRoleId());
                    return BusinessException.loginRoleNotFound();
                });

        // 更新最后登录时间
        user.setLastLoginTime(Instant.now());
        userRepository.save(user);

        // 检查是否需要密钥激活（仅针对普通用户）
        boolean needActivation = false;
        boolean isActivated = user.getIsActivated() != null && user.getIsActivated();
        if (NORMAL_USER_ROLE_ID.equals(user.getRoleId()) && isActivationRequired()) {
            needActivation = !isActivated;
        }

        // 生成JWT Token
        boolean rememberMe = request.getRememberMe() != null && request.getRememberMe();
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getId(),
                role.getRoleCode(),
                rememberMe,
                !needActivation
        );
        log.info("用户{}（手机号：{}）登录成功，记住我：{}，需要激活：{}，生成token（部分脱敏）：{}",
                user.getUsername(), user.getMobile(), rememberMe, needActivation, token.substring(0, 10) + "****");

        // 计算Token过期时间
        Long expireTime = System.currentTimeMillis() + (rememberMe ? jwtUtil.getRememberMeExpiration() : jwtUtil.getExpiration());

        // 判断是否需要修改初始密码：password_updated_at 为 null 表示从未修改过
        boolean requirePasswordChange = user.getPasswordUpdatedAt() == null;

        return LoginVO.builder()
                .token(token)
                .username(user.getUsername())
                .mobile(user.getMobile())
                .roleName(role.getRoleName())
                .createTime(user.getCreateTime() != null
                        ? user.getCreateTime().atZone(ZONE_ID_SHANGHAI).format(DATE_TIME_FORMATTER)
                        : null)
                .roleId(user.getRoleId())
                .userId(user.getId())
                .rememberMe(rememberMe)
                .avatar(user.getAvatar())
                .expireTime(expireTime)
                .guideCompleted(user.getGuideCompleted())
                .requirePasswordChange(requirePasswordChange)
                .needActivation(needActivation)
                .isActivated(user.getIsActivated())
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 注册流程：
     * <ol>
     *   <li>校验验证码是否正确</li>
     *   <li>校验两次输入的密码是否一致</li>
     *   <li>校验用户名是否已存在</li>
     *   <li>校验手机号格式及是否已存在（如有填写）</li>
     *   <li>校验默认角色是否存在</li>
     *   <li>创建新用户并保存（若开启密钥激活，则默认未激活）</li>
     *   <li>生成 JWT Token 并返回登录凭证（默认记住我）</li>
     * </ol>
     */
    @Override
    public LoginVO registerUser(RegisterRequest request, HttpServletRequest httpRequest) {
        log.info("普通用户注册请求，用户名：{}", request.getUsername());

        // 校验验证码
        if (request.getCaptchaId() == null || !captchaManager.verifyCaptcha(request.getCaptchaId(), request.getCaptcha())) {
            log.error("注册失败，验证码错误：{}，验证码ID：{}", request.getCaptcha(), request.getCaptchaId());
            throw BusinessException.loginCaptchaError();
        }

        // 校验两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.error("普通用户注册失败，用户名：{}两次密码不一致", request.getUsername());
            throw BusinessException.registerPasswordNotMatch();
        }

        // 校验用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            log.error("普通用户注册失败，用户名：{}已存在", request.getUsername());
            throw BusinessException.registerUserExist();
        }

        String mobile = request.getMobile();
        // 将空白手机号统一转为 null，避免空字符串触发唯一键冲突
        if (mobile != null && mobile.isBlank()) {
            mobile = null;
        }

        // 校验手机号格式及唯一性
        if (mobile != null) {
            if (!PhoneUtil.isMobile(mobile)) {
                log.error("普通用户注册失败，手机号：{}格式错误", mobile);
                throw BusinessException.registerPhoneFormatError();
            }
            if (userRepository.existsByMobile(mobile)) {
                log.error("普通用户注册失败，手机号：{}已存在", mobile);
                throw BusinessException.registerPhoneExist();
            }
        }

        // 校验默认角色是否存在
        SysRole role = roleRepository.findById(NORMAL_USER_ROLE_ID)
                .orElseThrow(() -> {
                    log.error("普通用户注册失败，用户名：{}的角色ID：{}不存在", request.getUsername(), NORMAL_USER_ROLE_ID);
                    return BusinessException.registerUserRoleNotFound();
                });

        // 判断是否开启密钥激活
        boolean needActivation = isActivationRequired();

        // 构建并保存新用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMobile(mobile);
        user.setRoleId(NORMAL_USER_ROLE_ID);
        user.setCreateTime(Instant.now());
        user.setUpdateTime(Instant.now());
        user.setAvatar(DEFAULT_AVATAR_ID);
        user.setIsDeleted(false);
        user.setGuideCompleted(false);
        user.setIsActivated(!needActivation);
        user.setPasswordUpdatedAt(Instant.now());

        userRepository.save(user);
        log.info("普通用户注册成功，用户名：{}，手机号：{}，已激活：{}", request.getUsername(), mobile, user.getIsActivated());

        // 为新用户初始化最新重要通知
        notificationService.initializeImportantNoticeForNewUser(user.getId());

        // 注册成功后直接颁发登录凭证，默认记住我（rememberMe = true）
        boolean rememberMe = true;
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getId(),
                role.getRoleCode(),
                rememberMe,
                !needActivation
        );
        log.info("用户{}（手机号：{}）注册后自动登录，记住我：{}，需要激活：{}，生成token（部分脱敏）：{}",
                user.getUsername(), user.getMobile(), rememberMe, needActivation, token.substring(0, 10) + "****");

        Long expireTime = System.currentTimeMillis() + jwtUtil.getRememberMeExpiration();
        return LoginVO.builder()
                .token(token)
                .username(user.getUsername())
                .mobile(user.getMobile())
                .roleName(role.getRoleName())
                .createTime(user.getCreateTime() != null
                        ? user.getCreateTime().atZone(ZONE_ID_SHANGHAI).format(DATE_TIME_FORMATTER)
                        : null)
                .roleId(user.getRoleId())
                .userId(user.getId())
                .rememberMe(rememberMe)
                .avatar(user.getAvatar())
                .expireTime(expireTime)
                .guideCompleted(user.getGuideCompleted())
                .needActivation(needActivation)
                .isActivated(user.getIsActivated())
                .build();
    }

    /**
     * 检查系统是否开启了密钥激活功能。
     *
     * @return true 表示需要密钥激活
     */
    private boolean isActivationRequired() {
        return sysConfigRepository.findByConfigKey(KEY_REQUIRE_ACTIVATION)
                .map(config -> Boolean.parseBoolean(config.getConfigValue()))
                .orElse(false);
    }
}
