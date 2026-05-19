package com.yx.fridgebutler.service;

import com.yx.fridgebutler.vo.CapacityStatsVO;

/**
 * 容量利用率统计服务接口。
 * <p>提供冰箱容量利用率统计的查询与计算逻辑，支持AI估算与缓存机制。</p>
 */
public interface CapacityStatsService {

    /**
     * 获取当前用户的冰箱容量利用率统计。
     * <p>优先返回缓存数据，首次使用同步计算，过期缓存触发异步更新。</p>
     *
     * @param fridgeId 指定冰箱ID，为 null 时返回所有冰箱统计
     * @return 容量利用率统计结果，包含总体平均利用率及各冰箱明细
     */
    CapacityStatsVO getCapacityStats(Long fridgeId);
}
