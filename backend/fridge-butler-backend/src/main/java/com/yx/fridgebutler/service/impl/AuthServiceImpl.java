package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.LoginRequest;
import com.yx.fridgebutler.dto.LoginResponse;
import com.yx.fridgebutler.entity.SysRole;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.repository.SysRoleRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.AuthService;
import com.yx.fridgebutler.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        SysUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getIsDeleted() != null && user.getIsDeleted()) {
            throw new RuntimeException("账号已被禁用");
        }

        SysRole role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new RuntimeException("用户角色不存在"));

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getId(),
                role.getRoleCode()
        );

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .roleName(role.getRoleName())
                .userId(user.getId())
                .build();
    }
}
