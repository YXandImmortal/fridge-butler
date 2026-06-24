package com.yx.fridgebutler.dto.purchase;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购方案核对入库单项请求 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePlanSettleItemRequest {

    /** 方案物品ID；为空时表示本次新增的物品。 */
    private Long planItemId;

    /** 物品名称；新增物品时必填。 */
    private String itemName;

    /** 分类ID；新增物品时必填，修改原有物品时可选。 */
    private Long categoryId;

    /** 单位ID；新增物品时必填，修改原有物品时可选。 */
    private Long itemUnitId;

    /** 是否跳过。 */
    @NotNull(message = "skip 不能为空")
    private Boolean skip;

    /** 实际采购数量。 */
    private BigDecimal actualNum;

    /** 生产日期。 */
    private LocalDate productionDate;

    /** 保质期天数。 */
    private Integer shelfLifeDays;

    /** 存放位置。 */
    @Size(max = 100, message = "存放位置长度不能超过100")
    private String storageLocation;

    /** 备注。 */
    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;

    /**
     * 是否存入冰箱。
     * <p>传值时以该值为准；不传时，原有物品使用创建时的 storeInFridge，新增物品默认 true。</p>
     */
    private Boolean forceStoreInFridge;

    /** 非跳过时 actualNum 必须大于 0。 */
    @AssertTrue(message = "非跳过项的实际数量必须大于0")
    public boolean isActualNumValid() {
        if (Boolean.TRUE.equals(skip)) {
            return true;
        }
        return actualNum != null && actualNum.compareTo(BigDecimal.ZERO) > 0;
    }
}
