package com.yx.fridgebutler.dto.fridge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 冰箱创建请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FridgeCreateRequest {

    /**
     * 冰箱名称
     */
    @NotBlank(message = "冰箱名称不能为空")
    @Size(max = 30, message = "冰箱名称长度不能超过30")
    private String fridgeName;

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
     * 冰箱类型ID
     */
    private Long fridgeTypeId;

}