package com.b2c.cn.management.service.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Getter;

/**
 * @author zrq
 * 2026/2/24 14:28
 */
@Getter
public class RowCountListener extends AnalysisEventListener<Object> {
    private int rowCount = 0;

    @Override
    public void invoke(Object o, AnalysisContext analysisContext) {
        rowCount++;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
