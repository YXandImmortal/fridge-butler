package com.yx.fridgebutler.vo.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物品取出每日统计响应VO
 * <p>用于展示近30天每天的取出次数。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TakeOutDailyStatisticsVO {

    /**
     * 日期，格式：yyyy-MM-dd
     */
    private String date;

    /**
     * 当日取出次数（每条取出记录计为1次）
     */
    private Long count;
}
