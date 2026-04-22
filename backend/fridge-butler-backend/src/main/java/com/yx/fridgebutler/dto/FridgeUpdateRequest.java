package com.yx.fridgebutler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FridgeUpdateRequest {

    @NotNull(message = "冰箱ID不能为空")
    private Integer id;

    @NotBlank(message = "冰箱名称不能为空")
    @Size(max = 30, message = "冰箱名称长度不能超过30")
    private String fridgeName;

    @NotNull(message = "冰箱是否为默认不能为空")
    private Boolean isDefault;

    @Size(max = 200, message = "地址长度不能超过200")
    private String fridgeAddress;

    @Size(max = 200, message = "备注长度不能超过200")
    private String remark;

    private Integer totalCapacity;

    private Boolean status;
}
