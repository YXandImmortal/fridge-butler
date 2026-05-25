package com.yx.fridgebutler.vo.aichat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 热量计算结构化数据 VO。
 * <p>对应前端 calorie_calculation 消息类型的 data 字段。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalorieCalculationData {

    /** 总热量数值（必填） */
    private Integer totalCalories;

    /** 热量单位，默认"千卡" */
    private String unit;

    /** 份量说明，如 "1人份" / "每100g" */
    private String serving;

    /** 食材明细列表（必填，长度≥1） */
    private List<CalorieItem> items;

    /** 营养成分概览（可选） */
    private Map<String, String> nutrition;

    /** AI 总结文字（可选） */
    private String summary;
}
