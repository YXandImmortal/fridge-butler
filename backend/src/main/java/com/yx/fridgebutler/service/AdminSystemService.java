package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.admin.SystemConfigUpdateRequest;
import com.yx.fridgebutler.vo.admin.SystemConfigVO;

/**
 * 管理员系统配置服务接口
 */
public interface AdminSystemService {

    /**
     * 获取系统配置
     *
     * @return 系统配置 VO
     */
    SystemConfigVO getSystemConfig();

    /**
     * 更新系统配置
     *
     * @param request 配置更新请求
     */
    void updateSystemConfig(SystemConfigUpdateRequest request);
}
