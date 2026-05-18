package com.yx.fridgebutler.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

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
 * </p>
 */
@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String START_TIME_KEY = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 生成请求唯一追踪 ID
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put(TRACE_ID_KEY, traceId);

        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_KEY, startTime);

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
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_KEY);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();

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

        MDC.remove(TRACE_ID_KEY);
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
