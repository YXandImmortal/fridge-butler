package com.yx.fridgebutler.vo.dailytip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 每日小贴士响应 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTipVO {

    /**
     * 唯一标识
     */
    private Long id;

    /**
     * 类别标识：FACT / TIP / JOKE / RIDDLE
     */
    private String type;

    /**
     * 类别中文名称
     */
    private String typeLabel;

    /**
     * 表情符号
     */
    private String emoji;

    /**
     * 短标题
     */
    private String title;

    /**
     * 正文内容
     */
    private String content;

    /**
     * 日期字符串（yyyy-MM-dd）
     */
    private String date;

    /**
     * 谜语答案（非谜语为空字符串）
     */
    private String answer;
}
