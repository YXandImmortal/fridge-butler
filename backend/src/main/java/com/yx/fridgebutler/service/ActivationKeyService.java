package com.yx.fridgebutler.service;

import com.yx.fridgebutler.vo.LoginVO;

/**
 * 用户激活密钥服务接口
 */
public interface ActivationKeyService {

    /**
     * 验证并绑定激活密钥
     * <p>用户输入密钥后，校验密钥有效性并将其绑定到当前用户。</p>
     *
     * @param keyCode 密钥字符串
     * @param userId  当前用户ID
     * @return 登录信息（含新的 JWT Token）
     */
    LoginVO verifyKey(String keyCode, Long userId);

    /**
     * 查询用户激活状态
     *
     * @param userId 用户ID
     * @return true 表示已激活，false 表示未激活
     */
    boolean isUserActivated(Long userId);
}
