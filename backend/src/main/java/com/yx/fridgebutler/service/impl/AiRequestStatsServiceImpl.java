package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.entity.AiRequestLog;
import com.yx.fridgebutler.enums.AiProvider;
import com.yx.fridgebutler.enums.AiRequestStatus;
import com.yx.fridgebutler.repository.AiRequestLogRepository;
import com.yx.fridgebutler.service.AiRequestStatsService;
import com.yx.fridgebutler.vo.admin.AiRequestTrendVO;
import com.yx.fridgebutler.vo.admin.AiUsageStatsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiRequestStatsServiceImpl implements AiRequestStatsService {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Autowired
    private AiRequestLogRepository requestLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AiProvider provider, String model, AiRequestStatus status, Instant requestTime) {
        try {
            AiRequestLog requestLog = new AiRequestLog();
            requestLog.setProvider(provider);
            requestLog.setModel(model);
            requestLog.setStatus(status);
            requestLog.setCreateTime(requestTime != null ? requestTime : Instant.now());
            requestLogRepository.saveAndFlush(requestLog);
        } catch (Exception e) {
            log.error("AI 请求统计写入失败，厂商：{}，模型：{}，状态：{}", provider, model, status, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AiUsageStatsVO getUsage(Integer days) {
        int trendDays = days == null ? 7 : days;
        if (trendDays != 7 && trendDays != 30) {
            throw new IllegalArgumentException("统计天数仅支持 7 或 30");
        }

        LocalDate today = LocalDate.now(ZONE_ID);
        Instant todayStart = today.atStartOfDay(ZONE_ID).toInstant();
        Instant trendStart = today.minusDays(trendDays - 1L).atStartOfDay(ZONE_ID).toInstant();
        long[] total = readSummary(requestLogRepository.summarizeAll());
        long[] todaySummary = readSummary(requestLogRepository.summarizeSince(todayStart));

        Map<String, AiRequestTrendVO> trendMap = new HashMap<>();
        for (Object[] row : requestLogRepository.findTrendSince(trendStart)) {
            String date = row[0] != null ? row[0].toString() : "";
            trendMap.put(date, AiRequestTrendVO.builder()
                    .date(date)
                    .totalCount(toLong(row[1]))
                    .successCount(toLong(row[2]))
                    .failureCount(toLong(row[3]))
                    .build());
        }

        List<AiRequestTrendVO> trend = new ArrayList<>(trendDays);
        LocalDate startDate = today.minusDays(trendDays - 1L);
        for (int i = 0; i < trendDays; i++) {
            String date = startDate.plusDays(i).toString();
            trend.add(trendMap.getOrDefault(date, AiRequestTrendVO.builder()
                    .date(date)
                    .totalCount(0)
                    .successCount(0)
                    .failureCount(0)
                    .build()));
        }

        return AiUsageStatsVO.builder()
                .requestTotal(total[0])
                .requestToday(todaySummary[0])
                .successTotal(total[1])
                .failureTotal(total[2])
                .trend(trend)
                .build();
    }

    private long[] readSummary(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return new long[]{0, 0, 0};
        }
        Object[] row = rows.getFirst();
        return new long[]{toLong(row[0]), toLong(row[1]), toLong(row[2])};
    }

    private long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }
}
