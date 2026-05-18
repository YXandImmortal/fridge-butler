package com.yx.fridgebutler.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 冰箱信息响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FridgeVO {

    /**
     * 冰箱ID
     */
    private Long id;

    /**
     * 冰箱名称
     */
    private String fridgeName;

    /**
     * 是否为默认冰箱
     */
    private Boolean isDefault;

    /**
     * 冰箱地址
     */
    private String fridgeAddress;

    /**
     * 备注
     */
    private String remark;

    /**
     * 总容量
     */
    private Integer totalCapacity;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 物品数量
     */
    private Integer itemCount;
}