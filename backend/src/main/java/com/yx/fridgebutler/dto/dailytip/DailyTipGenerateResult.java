package com.yx.fridgebutler.dto.dailytip;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DeepSeek 生成每日小贴士的 JSON 响应映射。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyTipGenerateResult {

    /**
     * 类别：FACT / TIP / JOKE / RIDDLE
     */
    private String type;

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
     * 生成日期
     */
    private String date;

    /**
     * 谜语答案（非谜语为空字符串）
     */
    private String answer;
}
