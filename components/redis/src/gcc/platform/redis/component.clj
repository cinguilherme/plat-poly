(ns gcc.platform.redis.component
  (:require [com.stuartsierra.component :as component]
            [taoensso.carmine :as car :refer [wcar]]
            [schema.core :as s]
            [gcc.platform.redis.interface :as redis-component :refer [RedisComponentCore]]))

;; Define the schema for the Redis pool configuration
(def RedisPoolConfigSchema
  {:max-total s/Int
   :max-idle-per-key s/Int
   :min-idle-per-key s/Int
   :max-total-per-key s/Int
   :block-when-exhausted? s/Bool
   :max-wait-ms s/Int
   :test-on-borrow? s/Bool}) 

(defrecord RedisComponent [redis-spec]
  component/Lifecycle
  RedisComponentCore
  (start [this]
    (println "Redis component starting")
    this)
  (stop [this]
    (println "Redis component stopping")
    this)

  (set-key [this key value]
    (wcar (:redis-spec this) (car/set key value)))
  (get-key [this key]
    (wcar (:redis-spec this) (car/get key))))

(defrecord MockRedisComponent [data-atom]
  component/Lifecycle
  RedisComponentCore
  (start [this]
    (println "Mock Redis component starting")
    (assoc this :data-atom (atom {})))
  (stop [this]
    (println "Mock Redis component stopping")
    (assoc this :data-atom (atom {}))
    this)
  (set-key [this key value]
    (swap! (:data-atom this) assoc key value))
  (get-key [this key]
    (get @(:data-atom this) key)))

(defn new-mock-redis-component []
  (->MockRedisComponent (atom {})))

(s/defn new-redis-component :- RedisComponent
  "Creates a new RedisComponent with the given endpoint, port, and optional pool configuration."
  ([endpoint :- s/Str, port :- s/Int]
   (new-redis-component endpoint port nil))
  ([endpoint :- s/Str
    port :- s/Int
    pool-config :- (s/maybe RedisPoolConfigSchema)]
   (let [default-pool-config {:max-total 10
                              :max-idle-per-key 5
                              :min-idle-per-key 1
                              :max-total-per-key 8
                              :block-when-exhausted? true
                              :max-wait-ms 30000
                              :test-on-borrow? true}
         validated-pool-config (merge default-pool-config pool-config)
         redis-spec {:pool validated-pool-config
                     :spec {:host endpoint
                            :port port}}]
     (map->RedisComponent {:redis-spec redis-spec}))))

(comment

  (def redis-component (new-redis-component "localhost" 6379))
  (component/start redis-component)

  (def mock-redis (new-mock-redis-component))
  (component/start mock-redis)
  (redis-component/set-key mock-redis "1" "3")
  (redis-component/get-key mock-redis "1")

  (redis-component/get-key redis-component "1")

  (s/def redis-config :- RedisPoolConfigSchema
    {:max-total 10
     :max-idle-per-key 5
     :min-idle-per-key 1
     :max-total-per-key 8
     :block-when-exhausted? true
     :max-wait-ms 30000
     :test-on-borrow? true})

  (def redisc-extra (new-redis-component "localhost" 6379 redis-config))

  )