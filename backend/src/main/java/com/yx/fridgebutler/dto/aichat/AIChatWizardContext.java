package com.yx.fridgebutler.dto.aichat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * AI 向导上下文。
 * <p>用于多步骤交互向导（如冰箱创建向导），前端在向导流程中自动附加。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatWizardContext {

    /**
     * 向导类型。
     * <p>当前仅支持 "fridge_creation"（冰箱创建向导）。</p>
     */
    private String type;

    /**
     * 当前步骤索引（0-based）。
     */
    private Integer currentStep;

    /**
     * 已收集的表单数据。
     * <p>key 为字段名，value 为字段值。</p>
     */
    private Map<String, Object> formData;
}
