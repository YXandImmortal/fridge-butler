package com.yx.fridgebutler.dto.unittype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单位类型创建请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitTypeCreateRequest {

    /**
     * 单位类型名称
     */
    @NotBlank(message = "单位类型名称不能为空")
    @Size(max = 20, message = "单位类型名称长度不能超过20")
    private String typeName;
}
