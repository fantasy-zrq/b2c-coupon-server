package com.b2c.cn.starter.config;

import com.b2c.cn.starter.annotation.aspect.RegularCheckChainFilterAspect;
import com.b2c.cn.starter.chain.ChainFilterContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author zrq
 * 2026/2/12 9:39
 */
@AutoConfiguration
public class RegularCheckChainFilterConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RegularCheckChainFilterAspect regularCheckChainFilterAspect(ChainFilterContext chainFilterContext) {
        return new RegularCheckChainFilterAspect(chainFilterContext);
    }

    @Bean
    public ChainFilterContext chainFilterContext() {
        return new ChainFilterContext();
    }
}
