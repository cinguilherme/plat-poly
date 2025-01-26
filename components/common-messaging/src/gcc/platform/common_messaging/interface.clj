(ns gcc.platform.common-messaging.interface
  (:require [gcc.platform.common-messaging.in-mem-event-bus.component :as in-mem-event-bus]
            [gcc.platform.common-messaging.core-async.component :as core-async]
            [gcc.platform.common-messaging.rabbitmq.component :as rabbitmq]
            [gcc.platform.common-messaging.redis.component :as redis]
            [gcc.platform.common-messaging.core :as core]))

(defn new-producer-component [{:keys [kind] :as settings}])

(defn create-new-in-memory-consumer [{:keys [bus threads-atom stop?-atom]}]
  (let [bus (or bus (ex-info "bus is required" {}))
        threads-atom (or threads-atom (atom {}))
        stop?-atom (or stop?-atom (atom false))]
    (in-mem-event-bus/create-in-mem-consumer bus threads-atom stop?-atom)))

(defn create-new-in-mem-core-async-consumer [{:keys [channels]}]
  (let [channels (or channels (atom {}))]
    (core-async/create-core-async-consumer channels)))

(defn create-new-in-memory-producer [configs]
  (in-mem-event-bus/create-in-mem-producer))

(defn create-new-in-mem-core-async-producer [configs]
  (core-async/create-core-async-producer))

(defn new-producer-component [{:keys [kind configs] :as settings}]
  (when (core/exists? core/kind-set kind)
    (throw (IllegalArgumentException. (str "Invalid kind: " kind))))
  (cond-> (case kind
            :in-mem        (create-new-in-memory-producer configs)
            ;:in-mem-async  (core-async/create-core-async-producer)
            ;:rabbitmq     (rabbitmq/new-producer-component settings)
            :redis         (redis/create-redis-producer))))

(defn new-consumer-component [{:keys [kind configs] :as settings}]
  (when (core/exists? core/kind-set kind)
    (throw (IllegalArgumentException. (str "Invalid kind: " kind))))
  (cond-> (case kind
            :in-mem        (create-new-in-memory-consumer configs)
            ;:in-mem-async  (core-async/create-core-async-consumer)
            ;:rabbitmq     (rabbitmq/new-consumer-component settings)
            :redis         (redis/create-redis-consumer))))