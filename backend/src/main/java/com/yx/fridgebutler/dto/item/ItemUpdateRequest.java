package com.yx.fridgebutler.dto.item;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 物品更新请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemUpdateRequest {

    /**
     * 物品ID
     */
    @NotNull(message = "物品ID不能为空")
    private Long id;

    /**
     * 物品名称
     */
    @NotBlank(message = "物品名称不能为空")
    @Size(max = 50, message = "物品名称长度不能超过50")
    private String itemName;

    /**
     * 物品单位ID
     */
    private Long itemUnitId;

    /**
     * 入库日期
     */
    private LocalDate storedDate;

    /**
     * 生产日期
     */
    private LocalDate productionDate;

    /**
     * 保质期天数
     */
    private Integer shelfLifeDays;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0.01", message = "数量必须大于0")
    private BigDecimal itemNum;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;

    /**
     * 存储位置
     */
    @Size(max = 50, message = "存储位置长度不能超过50")
    private String storageLocation;

}