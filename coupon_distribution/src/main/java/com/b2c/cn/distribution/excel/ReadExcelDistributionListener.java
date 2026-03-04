package com.b2c.cn.distribution.excel;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import com.b2c.cn.distribution.common.constant.RedisStoreConstant;
import com.b2c.cn.distribution.common.mqsendtemplate.CouponTemplateDistributionEvent;
import com.b2c.cn.distribution.common.mqsendtemplate.RocketMQCouponTaskDistributionTemplate;
import com.b2c.cn.distribution.dao.entity.CouponTaskDO;
import com.b2c.cn.distribution.dao.entity.CouponTaskFailDO;
import com.b2c.cn.distribution.dao.entity.CouponTemplateDO;
import com.b2c.cn.distribution.dao.mapper.CouponTaskFailMapper;
import com.b2c.cn.distribution.utils.StockDecrementReturnCombinedUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Map;

import static com.b2c.cn.distribution.common.constant.RedisStoreConstant.COUPON_TASK_PROGRESS_KEY;

/**
 * @author zrq
 * 2026/3/3 15:16
 */
@RequiredArgsConstructor
public class ReadExcelDistributionListener extends AnalysisEventListener<CouponTaskExcelObject> {

    private final CouponTaskFailMapper couponTaskFailMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final CouponTaskDO couponTaskDO;
    private final CouponTemplateDO couponTemplateDO;
    private final RocketMQCouponTaskDistributionTemplate rocketMQCouponTaskDistributionTemplate;

    private int rowCount = 1;
    private final static String STOCK_DECREMENT_AND_BATCH_SAVE_USER_RECORD_LUA_PATH = "lua/stock_decrement_and_batch_save_user_record.lua";
    private final static int BATCH_USER_COUPON_SIZE = 5000;

    @Override
    public void invoke(CouponTaskExcelObject excelHeader, AnalysisContext analysisContext) {

        String taskProgressKey = String.format(COUPON_TASK_PROGRESS_KEY, couponTaskDO.getId());
        String progressValue = stringRedisTemplate.opsForValue().get(taskProgressKey);
        //防止重复分发
        if (StrUtil.isNotBlank(progressValue) && Integer.parseInt(progressValue) > rowCount) {
            rowCount++;
            return;
        }

        // 获取 LUA 脚本，并保存到 Hutool 的单例管理容器，下次直接获取不需要加载
        DefaultRedisScript<Long> buildLuaScript = Singleton.get(STOCK_DECREMENT_AND_BATCH_SAVE_USER_RECORD_LUA_PATH, () -> {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(STOCK_DECREMENT_AND_BATCH_SAVE_USER_RECORD_LUA_PATH)));
            redisScript.setResultType(Long.class);
            return redisScript;
        });

        // 执行 LUA 脚本进行扣减库存以及增加 Redis 用户领券记录
        String couponTemplateKey = String.format(RedisStoreConstant.COUPON_TEMPLATE_KEY, couponTemplateDO.getId());
        String batchUserSetKey = String.format(RedisStoreConstant.COUPON_USER_RECEIVE_KEY, couponTaskDO.getId());
        Map<Object, Object> userRowNumMap = MapUtil.builder()
                .put("userId", excelHeader.getUserId())
                .put("rowNum", rowCount + 1)
                .build();
        Long combinedFiled = stringRedisTemplate.execute(
                buildLuaScript,
                ListUtil.of(couponTemplateKey, batchUserSetKey),
                JSON.toJSONString(userRowNumMap)
        );

        //b为true代表扣减库存成功
        boolean b = StockDecrementReturnCombinedUtil.extractFirstField(combinedFiled);
        if (!b) {
            stringRedisTemplate.opsForValue().set(taskProgressKey, String.valueOf(rowCount));
            rowCount++;
            Map<Object, Object> failMap = MapUtil.builder()
                    .put("userId", excelHeader.getUserId())
                    .put("rowNum", rowCount)
                    .put("cause", "优惠券模板库存不足")
                    .build();
            CouponTaskFailDO failDO = CouponTaskFailDO.builder()
                    .batchId(couponTaskDO.getId())
                    .jsonObject(JSON.toJSONString(failMap))
                    .build();
            couponTaskFailMapper.insert(failDO);
            return;
        }
        int receiveUserNumber = StockDecrementReturnCombinedUtil.extractSecondField(combinedFiled.intValue());
        if (receiveUserNumber < BATCH_USER_COUPON_SIZE) {
            stringRedisTemplate.opsForValue().set(taskProgressKey, String.valueOf(rowCount));
            rowCount++;
            return;
        }
        CouponTemplateDistributionEvent event = CouponTemplateDistributionEvent.builder()
                .couponTaskId(couponTaskDO.getId())
                .couponTaskBatchId(couponTaskDO.getBatchId())
                .shopNumber(couponTemplateDO.getShopNumber())
                .couponTemplateId(couponTemplateDO.getId())
                .validEndTime(couponTemplateDO.getValidEndTime())
                .couponTemplateConsumeRule(couponTemplateDO.getConsumeRule())
                .batchUserSetSize(receiveUserNumber)
                .distributionEndFlag(false)
                .build();
        rocketMQCouponTaskDistributionTemplate.sendMessage(event);
        // 同步当前执行进度到缓存
        stringRedisTemplate.opsForValue().set(taskProgressKey, String.valueOf(rowCount));
        ++rowCount;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        CouponTemplateDistributionEvent event = CouponTemplateDistributionEvent.builder()
                .couponTaskId(couponTaskDO.getId())
                .couponTaskBatchId(couponTaskDO.getBatchId())
                .shopNumber(couponTemplateDO.getShopNumber())
                .couponTemplateId(couponTemplateDO.getId())
                .validEndTime(couponTemplateDO.getValidEndTime())
                .couponTemplateConsumeRule(couponTemplateDO.getConsumeRule())
                //.batchUserSetSize(receiveUserNumber)
                .distributionEndFlag(true)
                .build();
        rocketMQCouponTaskDistributionTemplate.sendMessage(event);
    }
}
