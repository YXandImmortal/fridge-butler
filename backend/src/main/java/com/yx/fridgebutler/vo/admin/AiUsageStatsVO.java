package com.yx.fridgebutler.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiUsageStatsVO {
    private long requestTotal;
    private long requestToday;
    private long successTotal;
    private long failureTotal;
    private List<AiRequestTrendVO> trend;
}
