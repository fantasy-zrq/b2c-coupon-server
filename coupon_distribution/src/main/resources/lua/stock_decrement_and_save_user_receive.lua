local couponTemplateKey = KEYS[1]
local limitPerPersonKey = KEYS[2]

local limitPersonNum = tonumber(ARGV[1])
local expireTime = tonumber(ARGV[2])

local function combineFields(firstField, secondField)
    -- 确定 SECOND_FIELD_BITS 为 14，因为 secondField 最大为 9999
    local SECOND_FIELD_BITS = 14

    -- 根据 firstField 的实际值，计算其对应的二进制表示
    -- 由于 firstField 的范围是0-2，我们可以直接使用它的值
    local firstFieldValue = firstField

    -- 模拟位移操作，将 firstField 的值左移 SECOND_FIELD_BITS 位
    local shiftedFirstField = firstFieldValue * (2 ^ SECOND_FIELD_BITS)

    -- 将 secondField 的值与位移后的 firstField 值相加
    return shiftedFirstField + secondField
end

-- 获取当前库存
local stock = tonumber(redis.call('HGET', couponTemplateKey, 'stock'))

-- 判断库存是否大于 0
if stock <= 0 then
    return combineFields(1, 0) -- 库存不足
end

-- 获取用户领取的优惠券次数
local userCouponCount = tonumber(redis.call('GET', limitPerPersonKey))

-- 如果用户领取次数不存在，则初始化为 0
if userCouponCount == nil then
    userCouponCount = 0
end

-- 判断用户是否已经达到领取上限
if userCouponCount >= limitPersonNum then
    return combineFields(2, userCouponCount) -- 用户已经达到领取上限
end

-- 增加用户领取的优惠券次数
if userCouponCount == 0 then
    -- 如果用户第一次领取，则需要添加过期时间
    redis.call('SET', limitPerPersonKey, 1)
    redis.call('EXPIREAT', limitPerPersonKey, expireTime)
else
    -- 因为第一次领取已经设置了过期时间，第二次领取沿用之前即可
    redis.call('INCR', limitPerPersonKey)
end

-- 减少优惠券库存
redis.call('HINCRBY', couponTemplateKey, 'stock', -1)

return combineFields(0, userCouponCount + 1)
