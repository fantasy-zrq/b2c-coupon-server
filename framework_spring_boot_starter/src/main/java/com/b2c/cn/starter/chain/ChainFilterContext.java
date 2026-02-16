package com.b2c.cn.starter.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zrq
 * 2026/2/10 13:59
 */
@Slf4j(topic = "ChainFilterContext")
public class ChainFilterContext implements CommandLineRunner, ApplicationContextAware {
    private ApplicationContext applicationContext;
    private final Map<String, List<ChainFilterAbstractDefine>> chainFilterMap = new HashMap<>();

    @Override
    public void run(String... args) throws Exception {
        Map<String, ChainFilterAbstractDefine> chainMap = applicationContext.getBeansOfType(ChainFilterAbstractDefine.class);
        chainMap.forEach((beanName, bean) -> {
            String mark = bean.mark();
            List<ChainFilterAbstractDefine> filterList = chainFilterMap.getOrDefault(mark, new ArrayList<>());
            filterList.add(bean);
            chainFilterMap.put(mark, filterList);
            log.info("校验链：{}-装载完成", beanName);
        });
    }

    public <T> void handler(String mark, T requestParam) {
        List<ChainFilterAbstractDefine> filterList = chainFilterMap.get(mark);
        if (filterList != null && !filterList.isEmpty()) {
            filterList.forEach(each -> {
                log.info("校验链：{}-开始执行", each.mark());
                each.handler(requestParam);
            });
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
