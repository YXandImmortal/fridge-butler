package com.yx.fridgebutler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * DeepSeek AI 大模型配置类。
 * <p>提供调用 DeepSeek API 所需的 RestTemplate Bean。</p>
 */
@Configuration
public class DeepSeekConfig {

    /**
     * 创建用于调用 DeepSeek API 的 RestTemplate。
     *
     * @return RestTemplate 实例
     */
    @Bean
    public RestTemplate deepSeekRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(180000);
        return new RestTemplate(factory);
    }
}
