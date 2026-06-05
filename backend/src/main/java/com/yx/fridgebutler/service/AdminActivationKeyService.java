package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.admin.ActivationKeyGenerateRequest;
import com.yx.fridgebutler.dto.admin.ActivationKeyQueryRequest;
import com.yx.fridgebutler.vo.admin.ActivationKeyVO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 管理员激活密钥服务接口
 */
public interface AdminActivationKeyService {

    /**
     * 批量生成激活密钥
     *
     * @param request 生成请求参数
     * @return 生成的密钥列表
     */
    List<ActivationKeyVO> generateKeys(ActivationKeyGenerateRequest request);

    /**
     * 分页查询激活密钥列表
     *
     * @param request 查询请求参数
     * @return 密钥分页数据
     */
    Page<ActivationKeyVO> getKeyList(ActivationKeyQueryRequest request);

    /**
     * 收回激活密钥
     * <p>将密钥状态改为 REVOKED，并将已绑定的用户取消激活。</p>
     *
     * @param id 密钥ID
     */
    void revokeKey(Long id);

    /**
     * 发放激活密钥
     * <p>将 UNUSED 状态的密钥改为 ISSUED，表示已发放给用户。</p>
     *
     * @param id 密钥ID
     */
    void issueKey(Long id);

    /**
     * 销毁激活密钥
     * <p>仅允许销毁 UNUSED 或 ISSUED 状态的密钥。</p>
     *
     * @param id 密钥ID
     */
    void destroyKey(Long id);
}
