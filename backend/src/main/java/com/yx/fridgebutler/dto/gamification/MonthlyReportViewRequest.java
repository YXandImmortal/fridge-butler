package com.yx.fridgebutler.dto.gamification;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 月度报告查看结算请求。
 * <p>前端确认用户真正查看月度报告后调用，用于发放首次查看 EXP。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportViewRequest {

    /** 报告年月，如 2026-05 */
    @NotBlank(message = "年月不能为空")
    private String yearMonth;
}
