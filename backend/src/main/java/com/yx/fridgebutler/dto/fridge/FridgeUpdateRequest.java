package com.yx.fridgebutler.dto.fridge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 冰箱更新请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FridgeUpdateRequest {

    /**
     * 冰箱ID
     */
    @NotNull(message = "冰箱ID不能为空")
    private Integer id;

    /**
     * 冰箱名称
     */
    @NotBlank(message = "冰箱名称不能为空")
    @Size(max = 30, message = "冰箱名称长度不能超过30")
    private String fridgeName;

    /**
     * 是否为默认冰箱
     */
    @NotNull(message = "冰箱是否为默认不能为空")
    private Boolean isDefault;

    /**
     * 冰箱地址
     */
    @Size(max = 200, message = "地址长度不能超过200")
    private String fridgeAddress;

    /**
     * 备注
     */
    @Size(max = 200, message = "备注长度不能超过200")
    private String remark;

    /**
     * 总容量
     */
    private Integer totalCapacity;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 冰箱类型ID
     */
    private Long fridgeTypeId;
}