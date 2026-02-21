package com.b2c.cn.management.dto.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zrq
 * 2026/2/18 13:39
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasePageRespDTO {
    /**
     * 当前页码
     */
    public long pageNo;

    /**
     * 每页数量
     */
    public long pageSize;

    /**
     * 总数
     */
    public long total;
}
