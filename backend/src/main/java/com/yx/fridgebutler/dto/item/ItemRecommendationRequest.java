package com.yx.fridgebutler.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物品 AI 推荐请求 DTO。
 * <p>用户输入物品名称，由后端调用 AI 推荐分类、单位、存储位置、存放日期等字段。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRecommendationRequest {

    /**
     * 物品名称。
     */
    @NotBlank(message = "物品名称不能为空")
    @Size(max = 50, message = "物品名称长度不能超过50")
    private String itemName;

    /**
     * 冰箱 ID，可选。
     * <p>传入时可用于进一步校验冰箱归属，第一版主要用于日志记录。</p>
     */
    private Long fridgeId;

}
