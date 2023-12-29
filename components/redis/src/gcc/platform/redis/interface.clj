(ns gcc.platform.redis.interface)


(defprotocol RedisComponentCore
  (set-key [component key value] "Sets a key-value pair in Redis.")
  (get-key [component key] "Gets a value by key from Redis."))