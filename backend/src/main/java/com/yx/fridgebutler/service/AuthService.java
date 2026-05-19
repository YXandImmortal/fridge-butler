package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.auth.LoginRequest;
import com.yx.fridgebutler.vo.LoginVO;
import com.yx.fridgebutler.dto.auth.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证服务接口。
 * <p>定义用户登录、注册等认证相关的业务逻辑。</p>
 */
public interface AuthService {

    /**
     * 用户登录。
     * <p>校验验证码、用户名/密码、账号状态，生成并返回 JWT Token。</p>
     *
     * @param request     登录请求参数
     * @param httpRequest HTTP 请求对象，用于获取客户端信息
     * @return 登录成功后的用户信息及 Token
     */
    LoginVO login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * 用户注册。
     * <p>校验验证码、密码一致性、用户名/手机号唯一性，创建新用户。</p>
     *
     * @param request     注册请求参数
     * @param httpRequest HTTP 请求对象，用于获取客户端信息
     */
    void registerUser(RegisterRequest request, HttpServletRequest httpRequest);
}
