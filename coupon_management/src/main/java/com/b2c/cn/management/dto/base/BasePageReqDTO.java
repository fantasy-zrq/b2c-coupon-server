package com.b2c.cn.management.dto.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zrq
 * 2026/2/18 13:38
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasePageReqDTO {
    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;
}
