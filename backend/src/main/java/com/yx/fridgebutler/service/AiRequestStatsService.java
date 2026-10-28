package com.yx.fridgebutler.service;

import com.yx.fridgebutler.enums.AiProvider;
import com.yx.fridgebutler.enums.AiRequestStatus;
import com.yx.fridgebutler.vo.admin.AiUsageStatsVO;

import java.time.Instant;

public interface AiRequestStatsService {

    void record(AiProvider provider, String model, AiRequestStatus status, Instant requestTime);

    AiUsageStatsVO getUsage(Integer days);
}
