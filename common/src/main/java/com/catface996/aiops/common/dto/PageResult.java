package com.catface996.aiops.common.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页结果类
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码（从 1 开始）
     */
    private Long current;

    /**
     * 每页记录数
     */
    private Long size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 当前页数据列表
     */
    private List<T> records;

    /**
     * 类型转换方法
     *
     * @param converter 转换函数
     * @param <R>       目标类型
     * @return 转换后的分页结果
     */
    public <R> PageResult<R> convert(Function<T, R> converter) {
        List<R> convertedRecords = records.stream()
                .map(converter)
                .collect(Collectors.toList());

        PageResult<R> result = new PageResult<>();
        result.setCurrent(this.current);
        result.setSize(this.size);
        result.setTotal(this.total);
        result.setPages(this.pages);
        result.setRecords(convertedRecords);
        return result;
    }
}
