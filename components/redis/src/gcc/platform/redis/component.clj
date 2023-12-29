(ns gcc.platform.redis.component
  (:require [com.stuartsierra.component :as component]
            [taoensso.carmine :as car :refer [wcar]]
            [cheshire.core :as json]
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

  ;; RedisComponentCore
  (set-key [this key value]
    ;; Serialize non-string values to JSON before storing
    (try 
      (let [value-str (if (string? value) value (json/generate-string value))]
        (wcar (:redis-spec this) (car/set key value-str)))
      (catch Exception _ value)))
  (get-key [this key]
    ;; Attempt to deserialize JSON back to original data type
    (let [value-str (wcar (:redis-spec this) (car/get key))]
      (try
        (json/parse-string value-str)
        (catch Exception _ value-str)))))

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
    (swap! (:data-atom this) assoc key value)
    "OK")
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
