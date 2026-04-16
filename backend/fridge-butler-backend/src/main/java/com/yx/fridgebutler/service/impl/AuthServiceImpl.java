package com.yx.fridgebutler.service.impl;

import cn.hutool.core.util.PhoneUtil;
import com.yx.fridgebutler.dto.LoginRequest;
import com.yx.fridgebutler.dto.LoginResponse;
import com.yx.fridgebutler.dto.RegisterRequest;
import com.yx.fridgebutler.entity.SysRole;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.SysRoleRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.AuthService;
import com.yx.fridgebutler.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private static final Long NORMAL_USER_ROLE_ID = 2L;

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private SysRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.remember-me-expiration:2592000000}")
    private Long rememberMeExpiration;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录请求，账号：{}", request.getAccount());

        SysUser user = userRepository.findByUsernameOrMobile(
                        request.getAccount() == null ? "" : request.getAccount().trim(),
                        request.getAccount() == null ? "" : request.getAccount().trim()
                )
                .orElseThrow(() -> {
                    log.error("登陆失败，用户不存在：账号：{}", request.getAccount());
                    return BusinessException.loginAuthFailed();
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("登陆失败，账号：{}密码：{}错误", request.getAccount(), request.getPassword());
            throw BusinessException.loginAuthFailed();
        }

        if (user.getIsDeleted() != null && user.getIsDeleted()) {
            log.error("登陆失败，账号：{}已被禁用", request.getAccount());
            throw BusinessException.loginForbidden();
        }

        SysRole role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> {
                    log.error("登陆失败，账号：{}的角色ID：{}不存在：", request.getAccount(), user.getRoleId());
                    return BusinessException.loginRoleNotFound();
                });

        boolean rememberMe = request.getRememberMe() != null && request.getRememberMe();
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getId(),
                role.getRoleCode(),
                rememberMe
        );
        log.info("用户{}（手机号：{}）登录成功，记住我：{}，生成token（部分脱敏）：{}",
                user.getUsername(), user.getMobile(), rememberMe, token.substring(0, 10) + "****");

        Long expireTime = System.currentTimeMillis() + (rememberMe ? rememberMeExpiration : expiration);
        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .roleName(role.getRoleName())
                .userId(user.getId())
                .rememberMe(rememberMe)
                .expireTime(expireTime)
                .build();
    }

    @Override
    public void registerUser(RegisterRequest request) {
        log.info("普通用户注册请求，用户名：{}", request.getUsername());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.error("普通用户注册失败，用户名：{}两次密码不一致", request.getUsername());
            throw BusinessException.registerPasswordNotMatch();
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.error("普通用户注册失败，用户名：{}已存在", request.getUsername());
            throw BusinessException.registerUserExist();
        }

        String mobile = request.getMobile();
        if (mobile != null && !mobile.isBlank()) {
            if (!PhoneUtil.isMobile(mobile)) {
                log.error("普通用户注册失败，手机号：{}格式错误", mobile);
                throw BusinessException.registerPhoneFormatError();
            }
            if (userRepository.existsByMobile(mobile)) {
                log.error("普通用户注册失败，手机号：{}已存在", mobile);
                throw BusinessException.registerPhoneExist();
            }
        }

        roleRepository.findById(NORMAL_USER_ROLE_ID)
                .orElseThrow(() -> {
                    log.error("普通用户注册失败，用户名：{}的角色ID：{}不存在", request.getUsername(), NORMAL_USER_ROLE_ID);
                    return BusinessException.registerUserRoleNotFound();
                });

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMobile(mobile);
        user.setRoleId(NORMAL_USER_ROLE_ID);
        user.setCreateTime(Instant.now());
        user.setUpdateTime(Instant.now());
        user.setIsDeleted(false);

        userRepository.save(user);
        log.info("普通用户注册成功，用户名：{}，手机号：{}", request.getUsername(), mobile);
    }
}
