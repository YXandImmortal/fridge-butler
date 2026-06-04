package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.entity.SysActivationKey;
import com.yx.fridgebutler.entity.SysRole;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.SysActivationKeyRepository;
import com.yx.fridgebutler.repository.SysRoleRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.ActivationKeyService;
import com.yx.fridgebutler.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 用户激活密钥服务实现类
 */
@Slf4j
@Service
public class ActivationKeyServiceImpl implements ActivationKeyService {

    @Autowired
    private SysActivationKeyRepository activationKeyRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private SysRoleRepository sysRoleRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * {@inheritDoc}
     * <p>验证密钥有效性，绑定到当前用户，并生成新的 JWT Token。</p>
     */
    @Override
    @Transactional
    public String verifyKey(String keyCode, Long userId) {
        // 查询密钥
        SysActivationKey key = activationKeyRepository.findByKeyCode(keyCode.toUpperCase())
                .orElseThrow(() -> {
                    log.warn("密钥验证失败，密钥不存在：{}，用户ID：{}", keyCode, userId);
                    return BusinessException.activationKeyInvalid();
                });

        // 校验密钥状态
        if (key.getStatus() == null) {
            log.warn("密钥验证失败，密钥状态为空：{}，用户ID：{}", keyCode, userId);
            throw BusinessException.activationKeyInvalid();
        }
        switch (key.getStatus()) {
            case "BOUND" -> {
                log.warn("密钥验证失败，密钥已被使用：{}，用户ID：{}", keyCode, userId);
                throw BusinessException.activationKeyAlreadyUsed();
            }
            case "REVOKED" -> {
                log.warn("密钥验证失败，密钥已被收回：{}，用户ID：{}", keyCode, userId);
                throw BusinessException.activationKeyRevoked();
            }
            case "DESTROYED" -> {
                log.warn("密钥验证失败，密钥已销毁：{}，用户ID：{}", keyCode, userId);
                throw BusinessException.activationKeyInvalid();
            }
        }

        if (!"UNUSED".equals(key.getStatus())) {
            log.warn("密钥验证失败，密钥状态异常：{}，状态：{}，用户ID：{}", keyCode, key.getStatus(), userId);
            throw BusinessException.activationKeyInvalid();
        }

        // 查询用户
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(BusinessException::userNotFound);

        // 绑定密钥到用户
        key.setStatus("BOUND");
        key.setBoundUserId(userId);
        key.setBoundTime(Instant.now());
        key.setUpdateTime(Instant.now());
        activationKeyRepository.save(key);

        // 激活用户
        user.setIsActivated(true);
        sysUserRepository.save(user);

        log.info("用户激活成功，用户ID：{}，用户名：{}，密钥：{}", userId, user.getUsername(), keyCode);

        // 生成新的 JWT Token（activated = true）
        return jwtUtil.generateToken(
                user.getUsername(),
                user.getId(),
                getRoleCode(user),
                true,
                true
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserActivated(Long userId) {
        return sysUserRepository.findById(userId)
                .map(user -> user.getIsActivated() != null && user.getIsActivated())
                .orElse(false);
    }

    /**
     * 获取用户角色编码
     *
     * @param user 用户实体
     * @return 角色编码
     */
    private String getRoleCode(SysUser user) {
        if (user.getRoleId() == null) {
            return "USER";
        }
        return sysRoleRepository.findById(user.getRoleId())
                .map(SysRole::getRoleCode)
                .orElse("USER");
    }
}
