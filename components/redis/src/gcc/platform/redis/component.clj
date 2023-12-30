(ns gcc.platform.redis.component
  (:require [com.stuartsierra.component :as component]
            [taoensso.carmine :as car :refer [wcar]]
            [cheshire.core :as json]
            [schema.core :as s]))

(defprotocol RedisComponentCore
  (set-key [component key value] "Sets a key-value pair in Redis. Both key and value must be string")
  (get-key [component key] "Gets a value by key from Redis. value is returned as raw string"))

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

  
  ;; RedisComponentCore

  (set-key [this key value] 
    (try 
      (let [value-str (if (string? value) value value)]
        (wcar (:redis-spec this) (car/set key value-str)))
      (catch Exception _ value)))
  
  (get-key [this key] 
    (let [value-str (wcar (:redis-spec this) (car/get key))]
      value-str)))

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
    (swap! (:data-atom this) assoc (str key) value)
    "OK")
  (get-key [this key] (get @(:data-atom this) key)))

(defn new-mock-redis-component 
  "Creates a new Mock Version of RedisComponent usin a single atom to mimic redis."
  [] 
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
