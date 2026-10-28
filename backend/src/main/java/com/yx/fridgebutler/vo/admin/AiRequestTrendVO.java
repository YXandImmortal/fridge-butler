package com.yx.fridgebutler.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequestTrendVO {
    private String date;
    private long totalCount;
    private long successCount;
    private long failureCount;
}
