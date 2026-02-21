package com.b2c.cn.management.dto.resp;

import com.b2c.cn.management.dao.entity.CouponTemplateDO;
import com.b2c.cn.management.dto.base.BasePageRespDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zrq
 * 2026/2/18 13:54
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponTemplatePageQueryRespDTO {

    /**
     * 基础分页响应信息
     */
    private BasePageRespDTO basePageRespDTO;

    /**
     * 数据列表
     */
    public List<CouponTemplateDO> records;
}
