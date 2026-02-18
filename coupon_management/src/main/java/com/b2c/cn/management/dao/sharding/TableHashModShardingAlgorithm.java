package com.b2c.cn.management.dao.sharding;

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
 * 全局槽位法 — 表路由算法
 * <p>
 * 核心公式:
 * totalSlots = dbCount * tablesPerDb (tablesPerDb =
 * availableTargetNames.size())
 * globalSlot = hash(shardingValue) % totalSlots
 * localTableIndex = globalSlot % tablesPerDb
 * <p>
 * 与 DBHashModShardingAlgorithm 配合使用，DB 取全局槽位的"商"，Table 取"余数"，
 * 两者基于同一个全局槽位的不同维度，完全独立，保证数据均匀分布。
 *
 * @author zrq
 *         2026/2/12 14:57
 */
public final class TableHashModShardingAlgorithm implements StandardShardingAlgorithm<Long> {

    @Getter
    private Properties props;

    private int dbCount;
    private static final String DB_COUNT_KEY = "db-count";

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        int tablesPerDb = availableTargetNames.size();
        int totalSlots = dbCount * tablesPerDb;
        int globalSlot = (int) (hashShardingValue(shardingValue.getValue()) % totalSlots);
        int localTableIndex = globalSlot % tablesPerDb;

        int index = 0;
        for (String targetName : availableTargetNames) {
            if (index == localTableIndex) {
                return targetName;
            }
            index++;
        }
        throw new IllegalArgumentException("No target found for value: " + shardingValue.getValue());
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames,
            RangeShardingValue<Long> shardingValue) {
        // 范围查询时返回所有表，让 ShardingSphere 在所有表中查询
        return List.copyOf(availableTargetNames);
    }

    @Override
    public void init(Properties props) {
        this.props = props;
        dbCount = getDbCount(props);
    }

    private int getDbCount(final Properties props) {
        ShardingSpherePreconditions.checkState(props.containsKey(DB_COUNT_KEY),
                () -> new ShardingAlgorithmInitializationException(getType(),
                        "db-count cannot be null."));
        return Integer.parseInt(props.getProperty(DB_COUNT_KEY));
    }

    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }
}