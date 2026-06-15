package com.yx.fridgebutler.util;

import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.SysUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 用户上下文工具类。
 * <p>封装从 Spring Security 上下文中获取当前登录用户信息的公共方法。</p>
 */
@Slf4j
@Component
public class UserContextUtil {

    private static UserContextUtil instance;

    @Autowired
    private SysUserRepository userRepository;

    @PostConstruct
    public void init() {
        instance = this;
    }

    /**
     * 获取当前登录用户的用户名。
     *
     * @return 当前用户名
     * @throws BusinessException 如果未获取到认证信息则抛出异常
     */
    public static String getCurrentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw BusinessException.notFound();
        }
        return authentication.getName();
    }

    /**
     * 获取当前登录用户的ID。
     *
     * @return 当前用户ID
     * @throws BusinessException 如果未获取到认证信息或用户不存在则抛出异常
     */
    public static Long getCurrentUserId() {
        String username = getCurrentUsername();
        SysUser user = instance.userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);
        return user.getId();
    }

    /**
     * 获取当前登录用户的完整实体。
     *
     * @return 当前用户实体
     * @throws BusinessException 如果未获取到认证信息或用户不存在则抛出异常
     */
    public static SysUser getCurrentUser() {
        String username = getCurrentUsername();
        return instance.userRepository.findByUsername(username)
                .orElseThrow(BusinessException::userNotFound);
    }
}
