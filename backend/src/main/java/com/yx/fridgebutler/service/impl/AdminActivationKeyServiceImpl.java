package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.dto.admin.ActivationKeyGenerateRequest;
import com.yx.fridgebutler.dto.admin.ActivationKeyQueryRequest;
import com.yx.fridgebutler.entity.SysActivationKey;
import com.yx.fridgebutler.entity.SysUser;
import com.yx.fridgebutler.exception.BusinessException;
import com.yx.fridgebutler.repository.SysActivationKeyRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.AdminActivationKeyService;
import com.yx.fridgebutler.vo.admin.ActivationKeyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 管理员激活密钥服务实现类
 */
@Slf4j
@Service
public class AdminActivationKeyServiceImpl implements AdminActivationKeyService {

    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String KEY_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int KEY_LENGTH = 8;
    private static final String KEY_PREFIX = "FB-";

    @Autowired
    private SysActivationKeyRepository activationKeyRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    /**
     * {@inheritDoc}
     * <p>批量生成指定数量的激活密钥，确保密钥码唯一。</p>
     */
    @Override
    @Transactional
    public List<ActivationKeyVO> generateKeys(ActivationKeyGenerateRequest request) {
        int count = request.getCount() != null ? request.getCount() : 1;
        String remark = request.getRemark();

        List<ActivationKeyVO> result = new ArrayList<>(count);
        int generated = 0;
        int maxAttempts = count * 100; // 防止无限循环
        int attempts = 0;

        while (generated < count && attempts < maxAttempts) {
            attempts++;
            String keyCode = generateUniqueKeyCode();
            if (keyCode == null) {
                continue;
            }

            SysActivationKey key = new SysActivationKey();
            key.setKeyCode(keyCode);
            key.setStatus("UNUSED");
            key.setRemark(remark);
            key.setCreateTime(Instant.now());
            key.setUpdateTime(Instant.now());

            activationKeyRepository.save(key);
            generated++;
            result.add(convertToVO(key));
        }

        log.info("管理员批量生成激活密钥成功，请求数量：{}，实际生成：{}", count, generated);
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>分页查询密钥列表，支持关键词和状态筛选。</p>
     */
    @Override
    public Page<ActivationKeyVO> getKeyList(ActivationKeyQueryRequest request) {
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
        Page<SysActivationKey> keyPage = activationKeyRepository.findByKeywordAndStatus(
                request.getKeyword(), request.getStatus(), pageable);

        return keyPage.map(this::convertToVO);
    }

    /**
     * {@inheritDoc}
     * <p>发放密钥：将 UNUSED 状态的密钥改为 ISSUED。</p>
     */
    @Override
    @Transactional
    public void issueKey(Long id) {
        SysActivationKey key = activationKeyRepository.findById(id)
                .orElseThrow(BusinessException::notFound);

        if (!"UNUSED".equals(key.getStatus())) {
            log.warn("发放密钥失败，密钥ID：{}当前状态：{}不允许发放", id, key.getStatus());
            throw BusinessException.activationKeyInvalid();
        }

        key.setStatus("ISSUED");
        key.setUpdateTime(Instant.now());
        activationKeyRepository.save(key);
        log.info("管理员发放激活密钥成功，密钥ID：{}，密钥码：{}", id, key.getKeyCode());
    }

    /**
     * {@inheritDoc}
     * <p>收回密钥：将状态改为 REVOKED，若已绑定用户则取消其激活状态。</p>
     */
    @Override
    @Transactional
    public void revokeKey(Long id) {
        SysActivationKey key = activationKeyRepository.findById(id)
                .orElseThrow(BusinessException::notFound);

        String status = key.getStatus();
        if (!"BOUND".equals(status) && !"UNUSED".equals(status) && !"ISSUED".equals(status)) {
            log.warn("收回密钥失败，密钥ID：{}当前状态：{}不允许收回", id, status);
            throw BusinessException.activationKeyInvalid();
        }

        // 如果已绑定用户，取消该用户的激活状态
        if (key.getBoundUserId() != null) {
            SysUser user = sysUserRepository.findById(key.getBoundUserId()).orElse(null);
            if (user != null) {
                user.setIsActivated(false);
                sysUserRepository.save(user);
                log.info("收回密钥成功，取消用户激活状态，用户ID：{}，用户名：{}", user.getId(), user.getUsername());
            }
        }

        key.setStatus("REVOKED");
        key.setUpdateTime(Instant.now());
        activationKeyRepository.save(key);
        log.info("管理员收回激活密钥成功，密钥ID：{}，密钥码：{}", id, key.getKeyCode());
    }

    /**
     * {@inheritDoc}
     * <p>仅允许销毁 UNUSED 或 ISSUED 状态的密钥。</p>
     */
    @Override
    @Transactional
    public void destroyKey(Long id) {
        SysActivationKey key = activationKeyRepository.findById(id)
                .orElseThrow(BusinessException::notFound);

        String status = key.getStatus();
        if (!"UNUSED".equals(status) && !"ISSUED".equals(status)) {
            log.warn("销毁密钥失败，密钥ID：{}当前状态：{}不允许销毁", id, status);
            throw BusinessException.activationKeyInvalid();
        }

        key.setStatus("DESTROYED");
        key.setUpdateTime(Instant.now());
        activationKeyRepository.save(key);
        log.info("管理员销毁激活密钥成功，密钥ID：{}，密钥码：{}", id, key.getKeyCode());
    }

    /**
     * 生成唯一密钥码
     *
     * @return 唯一的密钥字符串，如果尝试次数过多则返回 null
     */
    private String generateUniqueKeyCode() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            sb.append(KEY_CHARS.charAt(random.nextInt(KEY_CHARS.length())));
        }
        String keyCode = KEY_PREFIX + sb;

        if (activationKeyRepository.existsByKeyCode(keyCode)) {
            return null;
        }
        return keyCode;
    }

    /**
     * 将实体转换为 VO
     *
     * @param key 密钥实体
     * @return 密钥 VO
     */
    private ActivationKeyVO convertToVO(SysActivationKey key) {
        String boundUsername = null;
        if (key.getBoundUserId() != null) {
            boundUsername = sysUserRepository.findById(key.getBoundUserId())
                    .map(SysUser::getUsername)
                    .orElse(null);
        }

        return ActivationKeyVO.builder()
                .id(key.getId())
                .keyCode(key.getKeyCode())
                .status(key.getStatus())
                .boundUserId(key.getBoundUserId())
                .boundUsername(boundUsername)
                .boundTime(key.getBoundTime() != null
                        ? key.getBoundTime().atZone(ZONE_ID_SHANGHAI).format(DATE_TIME_FORMATTER)
                        : null)
                .remark(key.getRemark())
                .createTime(key.getCreateTime() != null
                        ? key.getCreateTime().atZone(ZONE_ID_SHANGHAI).format(DATE_TIME_FORMATTER)
                        : null)
                .build();
    }
}
