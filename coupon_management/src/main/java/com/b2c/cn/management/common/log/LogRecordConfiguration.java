package com.b2c.cn.management.common.log;

import cn.hutool.core.util.StrUtil;
import com.b2c.cn.management.common.context.UserMerchantContext;
import com.b2c.cn.management.common.enums.CouponTemplateTargetEnum;
import com.b2c.cn.management.common.enums.CouponTemplateTypeEnum;
import com.b2c.cn.management.dao.entity.CouponTemplateLogDO;
import com.b2c.cn.management.dao.mapper.CouponTemplateLogMapper;
import com.b2c.cn.starter.exception.ClientException;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.beans.Operator;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.ILogRecordService;
import com.mzt.logapi.service.IOperatorGetService;
import com.mzt.logapi.service.IParseFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author zrq
 * 2025/8/30 19:57
 */
@Configuration
@Slf4j
public class LogRecordConfiguration {

    /**
     * 作为获取用户上下文对象的方法
     */
    @Bean
    public IOperatorGetService getService() {
        return () -> new Operator("2025146689697873922");
    }

    /**
     * 将el表达式的信息进行关系映射
     */
    @Bean
    public IParseFunction parseFunction() {
        return new IParseFunction() {
            /**
             * @return 返回值代表记录方需要使用的函数名
             */
            @Override
            public String functionName() {
                return "COMMON_ENUM_PARSE";
            }

            /**
             *
             * @param value 函数入参,需要映射转换的值
             * @return 返回映射完成的值
             */
            @Override
            public String apply(Object value) {
                List<String> parts = StrUtil.split(String.valueOf(value), "_");
                if (parts.size() != 2) {
                    throw new ClientException("日志记录格式不正确");
                }
                try {
                    String enumClassName = parts.get(0);
                    String enumValue = parts.get(1);
                    if (enumClassName.equals("CouponTemplateTypeEnum")) {
                        return CouponTemplateTypeEnum.findValueByType(Integer.parseInt(enumValue));
                    } else if (enumClassName.equals("CouponTemplateTargetEnum")) {
                        return CouponTemplateTargetEnum.findValueByType(Integer.parseInt(enumValue));
                    } else {
                        throw new ClientException("不知名枚举");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("第二个下划线后面的值需要是整数。", e);
                }
            }
        };
    }

    /**
     * 将生成的log进行持久化
     */
    @Bean
    public ILogRecordService logRecordService(CouponTemplateLogMapper couponTemplateLogMapper) {

        return new ILogRecordService() {
            @Override
            public void record(LogRecord logRecord) {
                if (!"CouponTemplate".equals(logRecord.getType())) {
                    return;
                }
                try {
                    CouponTemplateLogDO logDO = CouponTemplateLogDO.builder()
                            .shopNumber(UserMerchantContext.getShopNumber())
                            .couponTemplateId(logRecord.getBizNo())
                            .operatorId(logRecord.getOperator())
                            .operationLog(logRecord.getAction())
                            .originalData(LogRecordContext.getVariable("originalData") == null ? "" :
                                    LogRecordContext.getVariable("originalData").toString())
                            .modifiedData(logRecord.getExtra() == null ? "" : logRecord.getExtra())
                            .build();
                    couponTemplateLogMapper.insert(logDO);
                    log.info("日志持久化成功--->>租户--{},日志号--{},日志操作--{},方法参数--{},dbUuid--{}",
                            logRecord.getTenant(), logRecord.getBizNo(), logRecord.getAction(), logRecord.getCodeVariable()
                            , LogRecordContext.getVariable("dbGenerateId"));
                } catch (Exception e) {
                    log.error("日志持久化失败--->>{}", e.getMessage());
                }
            }

            @Override
            public List<LogRecord> queryLog(String bizNo, String type) {
                return List.of();
            }

            @Override
            public List<LogRecord> queryLogByBizNo(String bizNo, String type, String subType) {
                return List.of();
            }
        };
    }
}
