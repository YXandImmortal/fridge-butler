package com.yx.fridgebutler.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemUnitVO {

    private Long id;

    private String unitName;

    private Long unitTypeId;

    private String unitTypeName;

    private Boolean isSystemDefault;
}
