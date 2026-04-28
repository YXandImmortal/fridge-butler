package com.yx.fridgebutler.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemSearchRequest {

    /**
     * 搜索关键字（模糊匹配物品名称）
     */
    private String keyword;

    /**
     * 分类ID筛选
     */
    private Long categoryId;

    /**
     * 单位ID筛选
     */
    private Long unitId;

    /**
     * 冰箱ID筛选
     */
    private Long fridgeId;

    /**
     * 排序字段：itemNum（数量）、storedDate（入库时间）
     */
    @Pattern(regexp = "itemNum|storedDate", message = "排序字段只能是 itemNum 或 storedDate")
    private String sortField = "storedDate";

    /**
     * 排序方向：asc（升序）、desc（降序）
     */
    @Pattern(regexp = "asc|desc", message = "排序方向只能是 asc 或 desc")
    private String sortOrder = "desc";
}
