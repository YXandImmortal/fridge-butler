package com.yx.fridgebutler.service;

import com.yx.fridgebutler.dto.admin.AdminLogQueryRequest;
import com.yx.fridgebutler.vo.PageResult;
import com.yx.fridgebutler.vo.admin.AdminLogVO;

/**
 * 管理员操作日志服务接口
 */
public interface AdminLogService {

    /**
     * 分页查询操作日志
     *
     * @param request 查询条件
     * @return 日志分页数据
     */
    PageResult<AdminLogVO> getLogList(AdminLogQueryRequest request);
}
