package com.b2c.cn.user.admin.dao.sharding;

import cn.hutool.core.lang.Singleton;
import lombok.Getter;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmInitializationException;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * 全局槽位法 — 数据库路由算法
 * <p>
 * 核心公式:
 * totalSlots = dbCount * tablesPerDb
 * globalSlot = hash(shardingValue) % totalSlots
 * dbIndex = globalSlot / tablesPerDb
 * <p>
 * 通过将库路由和表路由绑定到同一个全局槽位的不同"维度"（商 vs 余数），
 * 彻底避免了独立取模导致的奇偶耦合问题，保证数据在每个库和每个表上均匀分布。
 *
 * @author zrq
 *         2026/2/12 14:51
 */
public class DBHashModShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    @Getter
    private Properties props;

    private int tablesPerDb;
    private static final String TABLES_PER_DB_KEY = "tables-per-db";

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        int dbCount = availableTargetNames.size();
        int totalSlots = dbCount * tablesPerDb;
        int globalSlot = (int) (hashShardingValue(shardingValue.getValue()) % totalSlots);
        int dbIndex = globalSlot / tablesPerDb;

        int index = 0;
        for (String targetName : availableTargetNames) {
            if (index == dbIndex) {
                return targetName;
            }
            index++;
        }
        throw new IllegalArgumentException("No target found for value: " + shardingValue.getValue());
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames,
            RangeShardingValue<Long> shardingValue) {
        // 范围查询时返回所有数据源，让 ShardingSphere 在所有库中查询
        return List.copyOf(availableTargetNames);
    }

    @Override
    public void init(Properties props) {
        this.props = props;
        tablesPerDb = getTablesPerDb(props);
        Singleton.put(this);
    }

    private int getTablesPerDb(final Properties props) {
        ShardingSpherePreconditions.checkState(props.containsKey(TABLES_PER_DB_KEY),
                () -> new ShardingAlgorithmInitializationException(getType(),
                        "tables-per-db cannot be null."));
        return Integer.parseInt(props.getProperty(TABLES_PER_DB_KEY));
    }

    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }
}
