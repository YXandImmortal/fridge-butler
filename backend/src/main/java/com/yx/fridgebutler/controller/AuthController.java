package com.yx.fridgebutler.controller;

import com.yx.fridgebutler.dto.auth.LoginRequest;
import com.yx.fridgebutler.vo.LoginVO;
import com.yx.fridgebutler.dto.auth.RegisterRequest;
import com.yx.fridgebutler.enums.ResultCode;
import com.yx.fridgebutler.service.AuthService;
import com.yx.fridgebutler.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * <p>
 * 处理用户登录、注册等认证相关请求。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     *
     * @param request    登录请求参数，包含账号、密码、验证码等信息
     * @param httpRequest HTTP请求对象，用于获取客户端IP地址
     * @return 登录成功返回包含登录信息的响应结果
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        log.info("用户登录请求，账号：{}，客户端IP：{}", request.getAccount(), getClientIp(httpRequest));
        LoginVO response = authService.login(request, httpRequest);
        log.info("用户登录成功，账号：{}", request.getAccount());
        return Result.success(ResultCode.LOGIN_SUCCESS, response);
    }

    /**
     * 普通用户注册
     *
     * @param request    注册请求参数，包含用户名、密码、手机号等信息
     * @param httpRequest HTTP请求对象，用于获取客户端IP地址
     * @return 注册成功返回空数据的响应结果
     */
    @PostMapping("/register/user")
    public Result<Void> registerUser(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        log.info("普通用户注册请求，用户名：{}，客户端IP：{}", request.getUsername(), getClientIp(httpRequest));
        authService.registerUser(request, httpRequest);
        log.info("普通用户注册成功，用户名：{}", request.getUsername());
        return Result.success(ResultCode.REGISTER_SUCCESS, null);
    }

    /**
     * 获取客户端真实 IP 地址
     * <p>
     * 依次从 X-Forwarded-For、Proxy-Client-IP、WL-Proxy-Client-IP 请求头中获取，
     * 若均无法获取则使用 RemoteAddr。
     * </p>
     *
     * @param request HTTP请求对象
     * @return 客户端的真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
