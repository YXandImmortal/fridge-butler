package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.LoginRequest;
import com.yx.fridgebutler.dto.LoginResponse;
import com.yx.fridgebutler.entity.SysRole;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.SysRoleRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.AuthService;
import com.yx.fridgebutler.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private SysRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        log.info("用户登录请求，用户名：{}", request.getUsername());

        SysUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("登陆失败，用户不存在：{}", request.getUsername());
                    return BusinessException.authFailed("用户名或密码错误");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("登陆失败，用户名：{}密码错误：", request.getUsername());
            throw BusinessException.authFailed("用户名或密码错误");
        }

        if (user.getIsDeleted() != null && user.getIsDeleted()) {
            log.error("登陆失败，用户名：{}账号已被禁用：", request.getUsername());
            throw BusinessException.forbidden("账号已被禁用");
        }

        SysRole role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> {
                    log.error("登陆失败，用户名：{}的角色ID：{}不存在：", request.getUsername(), user.getRoleId());
                    return BusinessException.notFound("角色不存在");
                });

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getId(),
                role.getRoleCode()
        );
        log.info("用户{}登录成功，生成token（部分脱敏）：{}", request.getUsername(), token.substring(0, 10) + "****");

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .roleName(role.getRoleName())
                .userId(user.getId())
                .build();
    }
}
