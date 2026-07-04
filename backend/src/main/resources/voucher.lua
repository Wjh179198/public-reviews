local voucherId = ARGV[1]
local userId = ARGV[2]
local now = ARGV[3]

local stockKey = "voucher:stock" .. voucherId
local orderKey = "voucher:order" .. voucherId
local beginKey = "voucher:begin" .. voucherId
local endKey = "voucher:end" .. voucherId

local beginTime = redis.call('get', beginKey)
local endTime = redis.call('get', endKey)

if beginTime and now < beginTime then
    return 1
end

if endTime and now > endTime then
    return 2
end

if(tonumber(redis.call('get', stockKey)) <= 0) then
    return 3
end

if(redis.call('sismember', orderKey, userId) == 1) then
    return 4
end

redis.call('incrby', stockKey, -1)
redis.call('sadd', orderKey, userId)

return 0
