package com.yx.fridgebutler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 特殊场景提示词模板枚举。
 * <p>系统预设的特殊采购场景，用于 AI 特殊场景生成时读取对应的 Prompt 文件。</p>
 * <p>注意：此枚举与「用户采购计划模板」完全不同，不占用用户模板配额。</p>
 */
@Getter
@AllArgsConstructor
public enum SceneTemplate {

    HOTPOT("hotpot", "火锅", "scene-hotpot.md"),
    BBQ("bbq", "烧烤", "scene-bbq.md"),
    FAMILY_GATHERING("family", "家庭聚餐", "scene-family-gathering.md"),
    BABY_FOOD("baby", "宝宝辅食", "scene-baby-food.md");

    private final String code;
    private final String name;
    private final String promptFile;

    /**
     * 根据编码查找场景模板。
     *
     * @param code 编码
     * @return 枚举值，找不到返回 null
     */
    public static SceneTemplate fromCode(String code) {
        for (SceneTemplate template : values()) {
            if (template.getCode().equalsIgnoreCase(code)) {
                return template;
            }
        }
        return null;
    }
}
