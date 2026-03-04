local function combineFields(firstField, secondField)
    local firstFieldValue = firstField and 1 or 0
    return (firstFieldValue * 2 ^ 15) + secondField
end

local couponTemplateKey = KEYS[1]
local batchUserReceiveKey = KEYS[2]
local userIdRow = ARGV[1]

local stock = tonumber(redis.call("HGET", couponTemplateKey, "stock"));
if stock == nil or stock <= 0 then
    return combineFields(false, redis.call("SCARD", batchUserReceiveKey))
end

redis.call("HINCRBY", couponTemplateKey, "stock", -1)
redis.call("SADD", batchUserReceiveKey, userIdRow)
return combineFields(true, redis.call("SCARD", batchUserReceiveKey))
