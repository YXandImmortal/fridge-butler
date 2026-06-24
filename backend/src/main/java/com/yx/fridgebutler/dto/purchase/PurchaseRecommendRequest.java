package com.yx.fridgebutler.dto.purchase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 采购推荐请求 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRecommendRequest {

    /** 目标冰箱ID。 */
    @NotNull(message = "冰箱ID不能为空")
    private Long fridgeId;

    /** 推荐模式：daily 日常推荐 / special 特殊场景生成。 */
    @NotBlank(message = "推荐模式不能为空")
    @Size(max = 20, message = "推荐模式长度不能超过20")
    private String mode;

    /** 场景描述（special 模式时使用）。 */
    @Size(max = 255, message = "场景描述长度不能超过255")
    private String sceneDesc;

    /** 特殊场景模板编码（special 模式时使用）。 */
    @Size(max = 50, message = "场景模板编码长度不能超过50")
    private String sceneTemplate;

    /** 估计人数（special 模式时使用）。 */
    private Integer estimatedPeople;
}
