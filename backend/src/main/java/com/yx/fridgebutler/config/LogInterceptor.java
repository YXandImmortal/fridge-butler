package com.yx.fridgebutler.config;

import com.yx.fridgebutler.entity.SysOperLog;
import com.yx.fridgebutler.repository.SysOperLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP 请求日志拦截器
 * <p>
 * 统一记录所有 HTTP 请求和响应信息，包括：
 * <ul>
 *     <li>请求唯一追踪 ID（traceId）</li>
 *     <li>请求方法、URI、客户端 IP</li>
 *     <li>请求处理耗时</li>
 *     <li>响应状态码</li>
 * </ul>
 * 同时将日志信息异步持久化到数据库。
 * </p>
 */
@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String START_TIME_KEY = "startTime";

    @Autowired
    private SysOperLogRepository sysOperLogRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 生成请求唯一追踪 ID
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put(TRACE_ID_KEY, traceId);

        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_KEY, startTime);
        request.setAttribute(TRACE_ID_KEY, traceId);

        String clientIp = getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        if (queryString != null) {
            // 对查询参数中的敏感信息进行脱敏处理
            queryString = desensitizeQueryString(queryString);
            uri = uri + "?" + queryString;
        }

        log.info("[请求开始] {} {} | IP: {} | TraceId: {}", method, uri, clientIp, traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, @NonNull Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_KEY);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String traceId = (String) request.getAttribute(TRACE_ID_KEY);

        if (ex != null) {
            log.error("[请求异常] {} {} | 状态码: {} | 耗时: {}ms | 异常: {}",
                    method, uri, status, duration, ex.getMessage());
        } else if (status >= 500) {
            log.error("[请求结束] {} {} | 状态码: {} | 耗时: {}ms", method, uri, status, duration);
        } else if (status >= 400) {
            log.warn("[请求结束] {} {} | 状态码: {} | 耗时: {}ms", method, uri, status, duration);
        } else {
            log.info("[请求结束] {} {} | 状态码: {} | 耗时: {}ms", method, uri, status, duration);
        }

        // 异步持久化操作日志到数据库
        persistOperLog(request, response, ex, duration, traceId);

        MDC.remove(TRACE_ID_KEY);
    }

    /**
     * 异步保存操作日志到数据库
     */
    private void persistOperLog(HttpServletRequest request, HttpServletResponse response,
                                Exception ex, long duration, String traceId) {
        try {
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String params = queryString != null ? desensitizeQueryString(queryString) : null;

            // 获取当前登录用户信息
            Long userId = null;
            String username = null;
            try {
                Object uidObj = request.getAttribute("userId");
                if (uidObj instanceof Long) {
                    userId = (Long) uidObj;
                }
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()
                        && !"anonymousUser".equals(authentication.getPrincipal())) {
                    username = authentication.getName();
                }
            } catch (Exception e) {
                // 忽略获取用户信息的异常
            }

            // 未登录/公共接口请求时，根据 URI 显示友好占位
            if (username == null) {
                username = switch (uri) {
                    case "/api/auth/login" -> "登录请求";
                    case "/api/auth/register/user" -> "注册请求";
                    case "/api/captcha/generate" -> "请求验证码";
                    case "/api/captcha/verify" -> "验证码校验";
                    default -> "公共接口请求";
                };
            }

            SysOperLog operLog = new SysOperLog();
            operLog.setTraceId(traceId);
            operLog.setUserId(userId);
            operLog.setUsername(username);
            operLog.setMethod(request.getMethod());
            operLog.setUri(uri);
            operLog.setIp(getClientIp(request));
            operLog.setParams(params);
            operLog.setStatusCode(response.getStatus());
            operLog.setDurationMs((int) duration);
            operLog.setErrorMsg(ex != null ? ex.getMessage() : null);
            operLog.setCreateTime(Instant.now());

            CompletableFuture.runAsync(() -> {
                try {
                    sysOperLogRepository.save(operLog);
                } catch (Exception e) {
                    log.error("操作日志持久化失败：{}", e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("构建操作日志对象失败：{}", e.getMessage());
        }
    }

    /**
     * 获取客户端真实 IP 地址
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
        // 多个代理情况，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 对查询参数中的敏感信息进行脱敏
     */
    private String desensitizeQueryString(String queryString) {
        if (queryString == null) {
            return null;
        }
        // 对 password、token 等敏感字段的值进行脱敏
        return queryString.replaceAll("(?i)(password|token|secret|key|code)=([^&]*)", "$1=***");
    }
}
