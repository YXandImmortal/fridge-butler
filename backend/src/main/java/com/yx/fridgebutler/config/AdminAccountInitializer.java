package com.yx.fridgebutler.config;

import com.yx.fridgebutler.entity.SysRole;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.repository.SysRoleRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.util.SecurePasswordGenerator;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 管理员账号启动初始化器。
 * <p>应用启动时检测数据库中是否已存在超级管理员，若不存在则自动创建预设管理员账号，
 * 生成随机密码并输出到安全文件。该功能可通过配置开关控制。</p>
 */
@Component
@ConditionalOnProperty(
        prefix = "system.admin.auto-init",
        name = "enabled",
        havingValue = "true"
)
@Order(2)
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private SysRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminAutoInitProperties properties;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        log.info("========== 管理员账号自动初始化开始 ==========");

        List<AdminAutoInitProperties.Account> accounts = properties.getAccounts();

        // 若未配置任何账号，使用默认超级管理员
        if (accounts == null || accounts.isEmpty()) {
            AdminAutoInitProperties.Account defaultAccount = new AdminAutoInitProperties.Account();
            defaultAccount.setUsername("admin");
            defaultAccount.setRoleCode("SUPER_ADMIN");
            accounts = List.of(defaultAccount);
        }

        // 检查是否已有超级管理员存在
        boolean superAdminExists = userRepository.count() > 0 &&
                roleRepository.findByRoleCode("SUPER_ADMIN")
                        .map(role -> userRepository.findAll().stream()
                                .anyMatch(u -> role.getId().equals(u.getRoleId())))
                        .orElse(false);

        if (superAdminExists) {
            log.info("数据库中已存在超级管理员，跳过初始化。");
            log.info("========== 管理员账号自动初始化结束 ==========");
            return;
        }

        List<AccountResult> results = new ArrayList<>();

        for (AdminAutoInitProperties.Account account : accounts) {
            String username = account.getUsername();
            String roleCode = account.getRoleCode();

            if (username == null || username.isBlank()) {
                log.warn("账号配置中用户名为空，跳过该账号。");
                continue;
            }

            // 检查用户名是否已存在
            if (userRepository.existsByUsername(username)) {
                log.warn("用户名 [{}] 已存在，跳过创建。", username);
                continue;
            }

            // 查询角色
            SysRole role = roleRepository.findByRoleCode(roleCode)
                    .orElseThrow(() -> new IllegalStateException("角色不存在: " + roleCode));

            // 生成随机密码
            String plainPassword = SecurePasswordGenerator.generate();
            String encodedPassword = passwordEncoder.encode(plainPassword);

            // 创建用户
            SysUser user = new SysUser();
            user.setUsername(username);
            user.setPassword(encodedPassword);
            user.setRoleId(role.getId());
            user.setCreateTime(Instant.now());
            user.setUpdateTime(Instant.now());
            user.setAvatar("ice");
            user.setGuideCompleted(false);
            user.setIsDeleted(false);
            user.setIsActivated(true);
            user.setPasswordUpdatedAt(null);

            userRepository.save(user);

            results.add(new AccountResult(username, plainPassword, roleCode));
            log.info("管理员账号 [{}] 创建成功，角色: {}", username, roleCode);
        }

        if (!results.isEmpty()) {
            writeCredentialsFile(results);
        }

        log.info("========== 管理员账号自动初始化结束 ==========");
    }

    /**
     * 将账号密码明文写入安全文件。
     *
     * @param results 创建的账号结果列表
     */
    private void writeCredentialsFile(List<AccountResult> results) {
        try {
            Path dir = resolveOutputDir();
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            Path filePath = dir.resolve(properties.getFileName());

            StringBuilder content = new StringBuilder();
            content.append("========================================\n");
            content.append("  Fridge Butler 管理员账号初始化凭证\n");
            content.append("  生成时间: ").append(FORMATTER.format(Instant.now())).append("\n");
            content.append("  ⚠️  警告：请妥善保管此文件，登录后立即修改密码！\n");
            content.append("========================================\n\n");

            for (AccountResult result : results) {
                content.append("账号: ").append(result.username()).append("\n");
                content.append("密码: ").append(result.password()).append("\n");
                content.append("角色: ").append(result.roleCode()).append("\n");
                content.append("----------------------------------------\n");
            }

            content.append("\n请登录后立即修改初始密码以确保系统安全。\n");

            Files.writeString(filePath, content.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 尝试设置 POSIX 权限（Linux/Mac）
            try {
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
                Files.setPosixFilePermissions(filePath, perms);
            } catch (UnsupportedOperationException e) {
                // Windows 等非 POSIX 系统忽略
                log.debug("当前系统不支持 POSIX 文件权限设置");
            }

            log.warn("⚠️ 管理员初始凭证已写入文件: {}", filePath.toAbsolutePath());
            log.warn("⚠️ 请务必查看该文件并妥善保管，登录后立即修改密码！");

        } catch (IOException e) {
            log.error("管理员凭证文件写入失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 解析密码文件输出目录。
     *
     * @return 输出目录路径
     */
    private Path resolveOutputDir() {
        String outputDir = properties.getOutputDir();
        if (outputDir != null && !outputDir.isBlank()) {
            return Paths.get(outputDir);
        }
        // 默认使用系统临时目录下的 fridge-butler 子目录
        return Paths.get(System.getProperty("java.io.tmpdir"), "fridge-butler");
    }

    private record AccountResult(String username, String password, String roleCode) {
    }
}
