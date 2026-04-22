package com.yx.fridgebutler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FridgeCreateRequest {

    @NotBlank(message = "冰箱名称不能为空")
    @Size(max = 30, message = "冰箱名称长度不能超过30")
    private String fridgeName;

    @Size(max = 200, message = "地址长度不能超过200")
    private String fridgeAddress;

    @Size(max = 200, message = "备注长度不能超过200")
    private String remark;


}
