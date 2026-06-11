-- KEYS[1] race meta hash: race:{raceId}:meta
-- KEYS[2] race stock key: race:{raceId}:stock
-- KEYS[3] user registration marker: race:{raceId}:user:{userId}
-- ARGV[1] current epoch second
-- ARGV[2] marker ttl seconds
-- registrationMode: 1 lottery, 2 first come first served
local metaExists = redis.call('EXISTS', KEYS[1])
if metaExists == 0 then
    return 1
end

local enrollStart = tonumber(redis.call('HGET', KEYS[1], 'enrollStart'))
local enrollEnd = tonumber(redis.call('HGET', KEYS[1], 'enrollEnd'))
local registrationMode = tonumber(redis.call('HGET', KEYS[1], 'registrationMode'))
local now = tonumber(ARGV[1])

if enrollStart ~= nil and now < enrollStart then
    return 2
end

if enrollEnd ~= nil and now > enrollEnd then
    return 3
end

if redis.call('EXISTS', KEYS[3]) == 1 then
    return 4
end

if registrationMode == nil or registrationMode == 1 then
    redis.call('SET', KEYS[3], '1', 'EX', ARGV[2])
    return 0
end

local stock = tonumber(redis.call('GET', KEYS[2]))
if stock == nil then
    return 1
end

if stock <= 0 then
    return 5
end

redis.call('DECR', KEYS[2])
redis.call('SET', KEYS[3], '1', 'EX', ARGV[2])
return 0
