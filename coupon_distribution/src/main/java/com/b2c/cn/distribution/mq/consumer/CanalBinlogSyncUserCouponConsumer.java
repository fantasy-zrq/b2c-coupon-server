package com.b2c.cn.distribution.mq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.b2c.cn.distribution.common.constant.RedisStoreConstant;
import com.b2c.cn.distribution.common.constant.RocketMQStoreConstant;
import com.b2c.cn.distribution.common.mqsendtemplate.CanalBinlogEvent;
import com.b2c.cn.distribution.common.mqsendtemplate.UserCouponExpireCancelEvent;
import com.b2c.cn.distribution.mq.producer.RocketMQUserCouponExpireTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author zrq
 * 2026/3/6 16:45
 */
@RocketMQMessageListener(
        topic = RocketMQStoreConstant.COUPON_USER_CANAL_TOPIC,
        consumerGroup = RocketMQStoreConstant.COUPON_USER_CANAL_CONSUMER_GROUP
)
@RequiredArgsConstructor
@Slf4j(topic = "CanalBinlogSyncUserCouponConsumer")
@Component
public class CanalBinlogSyncUserCouponConsumer implements RocketMQListener<CanalBinlogEvent> {

    private final StringRedisTemplate stringRedisTemplate;
    private final RocketMQUserCouponExpireTemplate couponExpireTemplate;
    @Value("${coupon.save.type}")
    private String saveType;

    @Override
    public void onMessage(CanalBinlogEvent message) {
        if (!Objects.equals(saveType, "canal")) {
            log.info("CanalBinlogSyncUserCouponConsumer不消费该条消息，已经丢弃：{}", message);
            return;
        }
        Map<String, Object> first = CollUtil.getFirst(message.getData());
        String couponTemplateId = first.get("coupon_template_id").toString();
        String userCouponId = first.get("id").toString();
        // 只处理用户优惠券创建事件
        if (ObjectUtil.equal(message.getType(), "INSERT")) {
            // 添加用户领取优惠券模板缓存记录
            String userCouponListCacheKey = String.format(RedisStoreConstant.COUPON_USER_RECEIVE_LIST_KEY, first.get("user_id").toString());
            String userCouponItemCacheKey = StrUtil.builder()
                    .append(couponTemplateId)
                    .append("_")
                    .append(userCouponId)
                    .toString();
            Date receiveTime = DateUtil.parse(first.get("receive_time").toString());
            stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, receiveTime.getTime());

            // 由于 Redis 在持久化或主从复制的极端情况下可能会出现数据丢失，而我们对指令丢失几乎无法容忍，因此我们采用经典的写后查询策略来应对这一问题
            Double scored;
            try {
                scored = stringRedisTemplate.opsForZSet().score(userCouponListCacheKey, userCouponItemCacheKey);
                // scored 为空意味着可能 Redis Cluster 主从同步丢失了数据，比如 Redis 主节点还没有同步到从节点就宕机了，解决方案就是再新增一次
                if (scored == null) {
                    // 如果这里也新增失败了怎么办？我们大概率做不到绝对的万无一失，只能尽可能增加成功率
                    stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, receiveTime.getTime());
                }
            } catch (Throwable ex) {
                log.warn("查询Redis用户优惠券记录为空或抛异常，可能Redis宕机或主从复制数据丢失，基础错误信息：{}", ex.getMessage());
                // 如果直接抛异常大概率 Redis 宕机了，所以应该写个延时队列向 Redis 重试放入值。为了避免代码复杂性，这里直接写新增，大家知道最优解决方案即可
                stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, receiveTime.getTime());
            }

            // 发送延时消息队列，等待优惠券到期后，将优惠券信息从缓存中删除
            UserCouponExpireCancelEvent userCouponDelayCloseEvent = UserCouponExpireCancelEvent.builder()
                    .couponTemplateId(Long.parseLong(couponTemplateId))
                    .userCouponReceiveId(Long.parseLong(userCouponId))
                    .userId(Long.parseLong(first.get("user_id").toString()))
                    .delayTime(DateUtil.parse(first.get("valid_end_time").toString()).getTime())
                    .build();
            Boolean b = couponExpireTemplate.sendMessage(userCouponDelayCloseEvent);

            // 发送消息失败解决方案简单且高效的逻辑之一：打印日志并报警，通过日志搜集并重新投递
            if (!b) {
                log.warn("发送优惠券关闭延时队列失败，消息参数：{}", JSON.toJSONString(userCouponDelayCloseEvent));
            }
        }
    }
}
