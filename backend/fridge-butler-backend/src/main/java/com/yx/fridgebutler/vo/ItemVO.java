package com.yx.fridgebutler.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemVO {

    private Long id;

    private Long fridgeId;

    private String itemName;

    private Long itemUnitId;

    private String unitName;

    private Long unitTypeId;

    private String unitTypeName;

    private LocalDate storedDate;

    private LocalDate productionDate;

    private Integer shelfLifeDays;

    private Long categoryId;

    private String categoryName;

    private BigDecimal itemNum;

    private String remark;

    private String createTime;

    private String updateTime;
}
