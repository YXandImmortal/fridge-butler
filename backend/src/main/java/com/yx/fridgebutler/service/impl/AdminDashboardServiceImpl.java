package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.repository.BizFridgeItemRepository;
import com.yx.fridgebutler.repository.BizFridgeRepository;
import com.yx.fridgebutler.repository.SysUserRepository;
import com.yx.fridgebutler.service.AdminDashboardService;
import com.yx.fridgebutler.vo.admin.AdminDashboardStatsVO;
import com.yx.fridgebutler.vo.admin.AdminTrendVO;
import com.yx.fridgebutler.vo.admin.FridgeTypeDistributionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员数据看板服务实现类
 */
@Slf4j
@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final ZoneId ZONE_ID_SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private BizFridgeRepository bizFridgeRepository;

    @Autowired
    private BizFridgeItemRepository bizFridgeItemRepository;

    /**
     * {@inheritDoc}
     * <p>
     * 统计维度：
     * <ul>
     *     <li>用户总数：未删除的用户数量</li>
     *     <li>今日新增：创建时间为今日的用户数量</li>
     *     <li>冰箱总数：未删除的冰箱数量</li>
     *     <li>物品总数：未删除的物品数量</li>
     *     <li>今日活跃：今日有登录记录的用户数量</li>
     * </ul>
     * </p>
     */
    @Override
    public AdminDashboardStatsVO getStats() {
        Instant todayStart = LocalDate.now(ZONE_ID_SHANGHAI).atStartOfDay(ZONE_ID_SHANGHAI).toInstant();

        long userTotal = sysUserRepository.countByIsDeletedFalse();
        long userToday = sysUserRepository.countByIsDeletedFalseAndCreateTimeGreaterThanEqual(todayStart);
        long fridgeTotal = bizFridgeRepository.countByIsDeletedFalse();
        long itemTotal = bizFridgeItemRepository.countByIsDeletedFalse();
        long activeToday = sysUserRepository.countByIsDeletedFalseAndLastLoginTimeGreaterThanEqual(todayStart);

        // 查询冰箱类型分布
        List<Object[]> typeDistributionRaw = bizFridgeRepository.countFridgeGroupByType();
        List<FridgeTypeDistributionVO> fridgeTypeDistribution = typeDistributionRaw.stream()
                .map(row -> FridgeTypeDistributionVO.builder()
                        .name(row[0] != null ? (String) row[0] : "未分类")
                        .value(row[1] != null ? ((Number) row[1]).longValue() : 0L)
                        .build())
                .toList();

        log.info("管理员查询数据看板统计：用户总数={}, 今日新增={}, 冰箱总数={}, 物品总数={}, 今日活跃={}, 冰箱类型分布={}种",
                userTotal, userToday, fridgeTotal, itemTotal, activeToday, fridgeTypeDistribution.size());

        return AdminDashboardStatsVO.builder()
                .userTotal(userTotal)
                .userToday(userToday)
                .fridgeTotal(fridgeTotal)
                .itemTotal(itemTotal)
                .activeToday(activeToday)
                .fridgeTypeDistribution(fridgeTypeDistribution)
                .build();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 查询近 N 天的用户注册数和活跃数，缺失日期补零，保证返回连续 N 天的数据。
     * </p>
     */
    @Override
    public List<AdminTrendVO> getTrend(Integer days) {
        if (days == null || days < 1) {
            days = 7;
        }
        if (days > 90) {
            days = 90; // 限制最大查询 90 天
        }

        LocalDate today = LocalDate.now(ZONE_ID_SHANGHAI);
        LocalDate startDate = today.minusDays(days - 1);
        Instant startInstant = startDate.atStartOfDay(ZONE_ID_SHANGHAI).toInstant();

        // 查询注册趋势
        List<Object[]> newUserTrend = sysUserRepository.countNewUsersTrend(startInstant);
        Map<String, Long> newUserMap = new HashMap<>();
        for (Object[] row : newUserTrend) {
            String dateStr = row[0] != null ? row[0].toString() : "";
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            newUserMap.put(dateStr, count);
        }

        // 查询活跃趋势
        List<Object[]> activeUserTrend = sysUserRepository.countActiveUsersTrend(startInstant);
        Map<String, Long> activeUserMap = new HashMap<>();
        for (Object[] row : activeUserTrend) {
            String dateStr = row[0] != null ? row[0].toString() : "";
            Long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            activeUserMap.put(dateStr, count);
        }

        // 组装连续 N 天的数据，缺失日期补零
        List<AdminTrendVO> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            String dateStr = date.format(DATE_FORMATTER);
            result.add(AdminTrendVO.builder()
                    .date(dateStr)
                    .newUsers(newUserMap.getOrDefault(dateStr, 0L))
                    .activeUsers(activeUserMap.getOrDefault(dateStr, 0L))
                    .build());
        }

        log.info("管理员查询数据看板趋势：近{}天", days);
        return result;
    }
}
