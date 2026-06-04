package com.yx.fridgebutler.security;

import cn.hutool.json.JSONUtil;
import com.yx.fridgebutler.enums.ResultCode;
import com.yx.fridgebutler.util.JwtUtil;
import com.yx.fridgebutler.vo.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT 认证过滤器
 * <p>
 * 从请求头中解析 JWT Token，验证有效性后设置用户认证信息到 SecurityContext。
 * 继承自 {@link OncePerRequestFilter}，确保每个请求只被过滤一次。
 * </p>
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 不需要检查激活状态的白名单路径
     */
    private static final List<String> ACTIVATION_WHITELIST = List.of(
            "/activation-key/status",
            "/activation-key/verify",
            "/auth/logout",
            "/user/info",
            "/system/"
    );

    /**
     * 执行内部过滤逻辑
     * <p>
     * 从请求头中提取 Authorization 字段，解析 JWT Token 并验证其有效性。
     * 如果验证成功，将用户认证信息（用户名、角色）设置到 Spring Security 的上下文中。
     * 无论验证结果如何，都会继续执行过滤器链。
     * </p>
     *
     * @param request     HTTP 请求对象
     * @param response    HTTP 响应对象
     * @param filterChain 过滤器链
     * @throws ServletException 当发生 Servlet 相关异常时抛出
     * @throws IOException      当发生 IO 异常时抛出
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                String roleName = jwtUtil.extractRoleName(token);

                log.debug("JWT 验证成功，用户：{}，角色：{}，请求：{}", username, roleName, requestUri);

                Long userId = jwtUtil.extractUserId(token);
                request.setAttribute("userId", userId);

                // 检查激活状态：普通用户未激活且请求不在白名单中，则拦截
                boolean activated = jwtUtil.extractActivated(token);
                if (!activated && !isActivationWhitelist(requestUri) && !"SUPER_ADMIN".equals(roleName)) {
                    log.warn("用户未激活，拒绝访问，用户：{}，请求：{}", username, requestUri);
                    writeErrorResponse(response);
                    return;
                }

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.singletonList(authority)
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("JWT 验证失败，请求：{}", requestUri);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 判断请求路径是否在激活状态白名单中
     *
     * @param requestUri 请求URI
     * @return 在白名单中返回 true
     */
    private boolean isActivationWhitelist(String requestUri) {
        return ACTIVATION_WHITELIST.stream().anyMatch(requestUri::startsWith);
    }

    /**
     * 写入错误响应
     *
     * @param response HTTP 响应对象
     * @throws IOException IO 异常
     */
    private void writeErrorResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(ResultCode.USER_NOT_ACTIVATED.getCode(), ResultCode.USER_NOT_ACTIVATED.getMessage());
        response.getWriter().write(JSONUtil.toJsonStr(result));
    }
}
