package com.yx.fridgebutler.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** HTTP 请求日志拦截器 */
    @Autowired
    private LogInterceptor logInterceptor;

    /**
     * 注册拦截器
     * <p>
     * 将日志拦截器注册到所有请求路径，但排除错误处理路径。
     * </p>
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error");
    }
}
