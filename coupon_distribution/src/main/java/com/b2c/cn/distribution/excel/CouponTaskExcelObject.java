package com.b2c.cn.distribution.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author zrq
 * 2026/3/3 15:16
 */
@Data
public class CouponTaskExcelObject {

    @ExcelProperty("用户ID")
    private String userId;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("邮箱")
    private String mail;
}
