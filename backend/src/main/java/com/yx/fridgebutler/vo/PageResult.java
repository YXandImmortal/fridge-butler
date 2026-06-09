package com.yx.fridgebutler.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页结果包装 VO
 * <p>统一前后端分页接口响应格式，前端按 <code>list</code> + <code>total</code> 解析。</p>
 *
 * @param <T> 列表数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 当前页数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 将 Spring Data {@link org.springframework.data.domain.Page} 转换为 PageResult
     *
     * @param page Spring Data 分页对象
     * @param <T>  数据类型
     * @return PageResult 包装对象
     */
    public static <T> PageResult<T> of(org.springframework.data.domain.Page<T> page) {
        return PageResult.<T>builder()
                .list(page.getContent())
                .total(page.getTotalElements())
                .build();
    }

    /**
     * 手动构建分页结果。
     * <p>适用于非 Spring Data 分页场景，如自定义查询后手动组装分页数据。</p>
     *
     * @param list 当前页数据列表
     * @param total 总记录数
     * @param <T> 数据类型
     * @return PageResult 包装对象
     */
    public static <T> PageResult<T> of(List<T> list, Long total) {
        return PageResult.<T>builder()
                .list(list)
                .total(total)
                .build();
    }
}
