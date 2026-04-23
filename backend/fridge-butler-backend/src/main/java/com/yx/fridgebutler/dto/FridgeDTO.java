package com.yx.fridgebutler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FridgeDTO {

    private Long id;

    private String fridgeName;

    private Boolean isDefault;

    private String fridgeAddress;

    private String remark;

    private Integer totalCapacity;

    private Boolean status;

    private String createTime;

    private String updateTime;

    private Integer itemCount;
}
