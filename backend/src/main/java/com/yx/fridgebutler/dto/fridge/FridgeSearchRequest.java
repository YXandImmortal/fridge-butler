package com.yx.fridgebutler.dto.fridge;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 冰箱搜索请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FridgeSearchRequest {

    /**
     * 搜索关键字
     */
    private String keyword;

    /**
     * 排序字段：createTime（创建时间）、name（名称）、totalCapacity（总容量）
     */
    @Pattern(regexp = "createTime|name|totalCapacity", message = "排序字段只能是 createTime、name 或 totalCapacity")
    private String sortField = "createTime";

    /**
     * 排序方向：asc（升序）、desc（降序）
     */
    @Pattern(regexp = "asc|desc", message = "排序方向只能是 asc 或 desc")
    private String sortOrder = "desc";

    /**
     * 冰箱类型ID，可选筛选条件
     */
    private Long fridgeTypeId;
}