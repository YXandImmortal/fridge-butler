package com.yx.fridgebutler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * 冰箱管家后端应用启动类。
 * <p>Spring Boot 应用的入口，负责启动整个后端服务。</p>
 */
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class FridgeButlerBackendApplication {

    /**
     * 应用主入口方法。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FridgeButlerBackendApplication.class, args);
    }

}
