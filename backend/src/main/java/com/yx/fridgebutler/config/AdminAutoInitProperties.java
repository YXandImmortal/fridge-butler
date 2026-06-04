package com.yx.fridgebutler.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员账号自动初始化配置属性。
 * <p>绑定 application.yaml 中 system.admin.auto-init 前缀的配置项。</p>
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "system.admin.auto-init")
public class AdminAutoInitProperties {

    /**
     * 是否启用自动初始化，默认关闭。
     */
    private boolean enabled = false;

    /**
     * 预设管理员账号列表。
     */
    private List<Account> accounts = new ArrayList<>();

    /**
     * 凭证文件输出目录，为空则使用系统临时目录。
     */
    private String outputDir = "";

    /**
     * 凭证文件名。
     */
    private String fileName = "admin-credentials.txt";

    /**
     * 单个账号配置。
     */
    @Setter
    @Getter
    public static class Account {


        /**
         * 用户名。
         */
        private String username = "admin";

        /**
         * 角色编码，默认为超级管理员。
         */
        private String roleCode = "SUPER_ADMIN";

    }
}
