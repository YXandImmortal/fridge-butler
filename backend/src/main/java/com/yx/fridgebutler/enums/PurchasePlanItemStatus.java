package com.yx.fridgebutler.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 采购方案物品状态枚举。
 */
@Getter
@AllArgsConstructor
public enum PurchasePlanItemStatus {

    /** 待采购。 */
    PENDING((byte) 1, "待采购"),

    /** 已入库。 */
    STORED((byte) 2, "已入库"),

    /** 跳过。 */
    SKIPPED((byte) 3, "跳过"),

    /** 已采购但不入库。 */
    PURCHASED_NOT_STORED((byte) 4, "已采购不入库");

    private final byte code;
    private final String desc;

    /**
     * 根据编码查找枚举。
     *
     * @param code 编码
     * @return 枚举值，找不到返回 PENDING
     */
    public static PurchasePlanItemStatus fromCode(Byte code) {
        if (code == null) {
            return PENDING;
        }
        for (PurchasePlanItemStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return PENDING;
    }
}
