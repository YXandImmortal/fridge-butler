package com.yx.fridgebutler.config;

import com.yx.fridgebutler.entity.SysRole;
import com.yx.fridgebutler.repository.SysRoleRepository;
import org.jspecify.annotations.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统角色启动初始化器。
 * <p>应用启动时检测数据库中是否已存在预设角色，若不存在则自动创建系统默认角色。
 * 支持幂等执行：已存在的角色不会重复插入。</p>
 */
@Component
@ConditionalOnProperty(
        prefix = "system.role.init",
        name = "enabled",
        havingValue = "true"
)
@Order(1)
@Slf4j
public class SysRoleInitializer implements ApplicationRunner {



    /**
     * 系统预设角色列表。
     * 顺序很重要：第一条应为超级管理员，第二条应为普通用户，
     * 与业务代码中硬编码的 roleId 预期保持一致。
     */
    private static final List<RoleDef> DEFAULT_ROLES = List.of(
            new RoleDef("超级管理员", "SUPER_ADMIN", "超级管理员，拥有系统最高权限，由系统预设"),
            new RoleDef("普通用户", "USER", "拥有普通用户权限，可以通过注册创建")
    );

    /** 角色数据访问层 */
    @Autowired
    private SysRoleRepository roleRepository;

    /**
     * 执行系统角色自动初始化逻辑。
     *
     * @param args 应用启动参数
     */
    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        log.info("========== 系统角色自动初始化开始 ==========");

        // 查询已存在的角色
        List<SysRole> existingRoles = roleRepository.findAll();
        Map<String, SysRole> existingRoleMap = existingRoles.stream()
                .collect(Collectors.toMap(SysRole::getRoleCode, r -> r));

        List<SysRole> toSave = new ArrayList<>();
        for (RoleDef def : DEFAULT_ROLES) {
            if (!existingRoleMap.containsKey(def.roleCode())) {
                SysRole role = new SysRole();
                role.setRoleName(def.roleName());
                role.setRoleCode(def.roleCode());
                role.setRemark(def.remark());
                role.setUpdateTime(Instant.now());
                role.setIsDeleted(false);
                toSave.add(role);
                log.info("准备创建系统预设角色: {} ({})", def.roleName(), def.roleCode());
            } else {
                log.info("角色 [{}] 已存在，跳过创建。", def.roleCode());
            }
        }

        if (!toSave.isEmpty()) {
            List<SysRole> saved = roleRepository.saveAll(toSave);
            for (SysRole role : saved) {
                log.info("系统预设角色创建成功: id={}, roleName={}, roleCode={}",
                        role.getId(), role.getRoleName(), role.getRoleCode());
            }
            log.info("系统角色初始化完成，本次新增 {} 条。", saved.size());
        } else {
            log.info("所有系统预设角色已存在，跳过初始化。");
        }

        log.info("========== 系统角色自动初始化结束 ==========");
    }

    /**
     * 角色定义内部记录。
     */
    private record RoleDef(String roleName, String roleCode, String remark) {
    }
}
