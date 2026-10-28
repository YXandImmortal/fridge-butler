package com.yx.fridgebutler.service.impl;

import com.yx.fridgebutler.entity.AiRequestLog;
import com.yx.fridgebutler.enums.AiProvider;
import com.yx.fridgebutler.enums.AiRequestStatus;
import com.yx.fridgebutler.repository.AiRequestLogRepository;
import com.yx.fridgebutler.vo.admin.AiRequestTrendVO;
import com.yx.fridgebutler.vo.admin.AiUsageStatsVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiRequestStatsServiceImplTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Mock
    private AiRequestLogRepository requestLogRepository;

    @InjectMocks
    private AiRequestStatsServiceImpl requestStatsService;

    @Test
    void summarizesUsageAndFillsMissingTrendDates() {
        LocalDate today = LocalDate.now(ZONE_ID);
        when(requestLogRepository.summarizeAll()).thenReturn(List.<Object[]>of(new Object[]{10L, 7L, 3L}));
        when(requestLogRepository.summarizeSince(any())).thenReturn(List.<Object[]>of(new Object[]{2L, 2L, 0L}));
        when(requestLogRepository.findTrendSince(any())).thenReturn(List.<Object[]>of(
                new Object[]{Date.valueOf(today), 2L, 2L, 0L},
                new Object[]{Date.valueOf(today.minusDays(1)), 1L, 0L, 1L}
        ));

        AiUsageStatsVO result = requestStatsService.getUsage(7);

        assertEquals(10, result.getRequestTotal());
        assertEquals(2, result.getRequestToday());
        assertEquals(7, result.getSuccessTotal());
        assertEquals(3, result.getFailureTotal());
        assertEquals(7, result.getTrend().size());
        assertEquals(today.minusDays(6).toString(), result.getTrend().getFirst().getDate());
        AiRequestTrendVO todayTrend = result.getTrend().getLast();
        assertEquals(2, todayTrend.getTotalCount());
        assertEquals(2, todayTrend.getSuccessCount());
        assertEquals(0, todayTrend.getFailureCount());
        AiRequestTrendVO yesterdayTrend = result.getTrend().get(result.getTrend().size() - 2);
        assertEquals(1, yesterdayTrend.getFailureCount());
        assertEquals(0, result.getTrend().getFirst().getTotalCount());
    }

    @Test
    void supportsThirtyDayTrendAndRejectsOtherRanges() {
        LocalDate today = LocalDate.now(ZONE_ID);
        when(requestLogRepository.summarizeAll()).thenReturn(List.<Object[]>of(new Object[]{0L, 0L, 0L}));
        when(requestLogRepository.summarizeSince(any())).thenReturn(List.<Object[]>of(new Object[]{0L, 0L, 0L}));
        when(requestLogRepository.findTrendSince(any())).thenReturn(List.of());

        AiUsageStatsVO result = requestStatsService.getUsage(30);

        assertEquals(30, result.getTrend().size());
        assertEquals(today.minusDays(29).toString(), result.getTrend().getFirst().getDate());
        assertThrows(IllegalArgumentException.class, () -> requestStatsService.getUsage(14));
    }

    @Test
    void recordsRequestMetadataWithoutContent() {
        Instant requestTime = Instant.now();

        requestStatsService.record(AiProvider.QWEN, "qwen3.7-plus", AiRequestStatus.SUCCESS, requestTime);

        ArgumentCaptor<AiRequestLog> captor = ArgumentCaptor.forClass(AiRequestLog.class);
        verify(requestLogRepository).saveAndFlush(captor.capture());
        AiRequestLog saved = captor.getValue();
        assertEquals(AiProvider.QWEN, saved.getProvider());
        assertEquals("qwen3.7-plus", saved.getModel());
        assertEquals(AiRequestStatus.SUCCESS, saved.getStatus());
        assertEquals(requestTime, saved.getCreateTime());
    }

    @Test
    void statisticsFailureDoesNotInterruptAiCall() {
        when(requestLogRepository.saveAndFlush(any())).thenThrow(new RuntimeException("database unavailable"));

        assertDoesNotThrow(() -> requestStatsService.record(
                AiProvider.DEEPSEEK, "deepseek-v4-pro", AiRequestStatus.FAILURE, Instant.now()));
    }
}
