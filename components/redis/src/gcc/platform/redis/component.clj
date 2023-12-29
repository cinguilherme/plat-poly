(ns gcc.platform.redis.component
  (:require [com.stuartsierra.component :as component]
            [taoensso.carmine :as car :refer [wcar]]))

(defrecord RedisComponent [redis-spec]
  component/Lifecycle
  (start [this]
    (println "Elasticsearch component starting")
    this)
  (stop [this]
    (println "Elasticsearch component stopping")
    this))

(defn set-key [component key value]
  (wcar (:redis-spec component) (car/set key value)))

(defn get-key [component key]
  (wcar (:redis-spec component) (car/get key)))

(defn new-redis-component [endpoint port]
  (let [redis-spec {:pool {}
                    :spec {:host endpoint
                           :port port}}]
    (map->RedisComponent {:redis-spec redis-spec})))