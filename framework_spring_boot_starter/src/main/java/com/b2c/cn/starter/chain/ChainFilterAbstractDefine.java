package com.b2c.cn.starter.chain;

import org.springframework.core.Ordered;

/**
 * @author zrq
 * 2026/2/10 13:03
 */
public interface ChainFilterAbstractDefine<T> extends Ordered {

    void handler(T requestParam);

    String mark();
}
