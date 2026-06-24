package com.yx.fridgebutler.vo.purchase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 特殊场景提示词模板 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneTemplateVO {

    /** 模板编码。 */
    private String code;

    /** 模板名称。 */
    private String name;
}
