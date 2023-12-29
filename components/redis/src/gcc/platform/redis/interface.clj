(ns gcc.platform.redis.interface)


(defprotocol RedisComponentCore
  (set-key [component key value] "Sets a key-value pair in Redis. Both key and value must be string")
  (get-key [component key] "Gets a value by key from Redis. value is returned as raw string"))