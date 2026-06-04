package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.admin.AdminUserQueryRequest;
import com.yx.fridgebutler.vo.admin.AdminUserVO;
import org.springframework.data.domain.Page;

/**
 * 管理员用户管理服务接口
 */
public interface AdminUserService {

    /**
     * 分页查询用户列表
     *
     * @param request 查询条件
     * @return 用户分页数据
     */
    Page<AdminUserVO> getUserList(AdminUserQueryRequest request);

    /**
     * 查看用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    AdminUserVO getUserDetail(Long id);

    /**
     * 更新用户状态（启用/禁用）
     *
     * @param id     用户ID
     * @param status 状态：true=禁用，false=正常
     */
    void updateUserStatus(Long id, Boolean status);

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @return 新生成的明文密码
     */
    String resetUserPassword(Long id);
}
