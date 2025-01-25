(ns gcc.platform.common-messaging.redis.component
  (:require [com.stuartsierra.component :as component]
            [gcc.platform.common-messaging.interface :as intf]
            [taoensso.carmine :as car :refer [wcar]]
            [taoensso.carmine.message-queue :as car-mq]
            [clojure.pprint :as pprint]))

(defn tap [v]
  (pprint/pprint  v)
  v)

;- :min-idle-per-key  ; Min num of idle conns to keep per sub-pool (Default 0)
;- :max-idle-per-key  ; Max num of idle conns to keep per sub-pool (Default 16)
;- :max-total-per-key ; Max num of idle or active conns <...>      (Default 16)
(def poll-configs {})

(defonce my-conn-pool (car/connection-pool poll-configs)) ; Create a new stateful pool

;; (defmacro wcar* [& body] `(car/wcar my-wcar-opts ~@body))
(defn wcar* [opts & body] (car/wcar opts body))

(def default-producer-ops {:async? false})

(defrecord RedisProducer [server-info pool-settings]
  component/Lifecycle
  (start [this]
    (let [conn-spec {:uri (-> server-info :uri)}
          conn-pool (car/connection-pool pool-settings)
          wcar-opts {:pool conn-pool :spec conn-spec}]
      (assoc this :redis-producer {:wcar wcar-opts})))
  (stop [this]
    (dissoc this :redis-producer))

  intf/CommonProducer
  (send-message [this message ops]
    (let [opsx (tap (merge default-producer-ops ops))
          wcar-opts (-> this :redis-producer :wcar)
          payload (-> message :message)
          destination (-> message :destination)
          async? (-> opsx :async?)]
      (if async?
        (future (car/wcar wcar-opts (car-mq/enqueue (:queue destination) payload)))
        (car/wcar wcar-opts (car-mq/enqueue (:queue destination) payload)))))
  (send-messages [this messages ops]
    (let [opsx (merge default-producer-ops ops)
          async? (-> opsx :async?)]
      (mapv (fn [message]
              (let [wcar-opts (-> this :redis-producer :wcar)
                    payload (-> message :message)
                    destination (-> message :destination)]
                (if async?
                  (future (car/wcar wcar-opts (car-mq/enqueue (:queue destination) payload)))
                  (car/wcar wcar-opts (car-mq/enqueue (:queue destination) payload))))) messages))))

(defn create-redis-producer [server-info pool-settings]
  (->RedisProducer server-info pool-settings))

(declare start-list)
(defn start-list [list wcar-opts]
  (mapv (fn [settings]
          (println "starting consumer for settings" settings)
          (let [queue (-> settings :queue)
                callback (-> settings :handler)
                error-callback (-> settings :error-callback)]
            (car-mq/worker wcar-opts queue
                           {:handler callback
                            :error-callback error-callback})))
        list))

(defrecord RedisConsumer [server-info pool-settings]
  component/Lifecycle
  (start [this]
    (let [conn-spec {:uri (-> server-info :uri)}
          conn-pool (car/connection-pool pool-settings)
          wcar-opts {:pool conn-pool :spec conn-spec}] 
      (println "Starting Redis Consumer")
      (assoc this :redis-consumer {:wcar wcar-opts})))
  (stop [this]
    (println "Stopping Redis Consumer")
    ;; before dissoc the redis-consumer we need to stop all the consumers listeners in :redis-consumer :consumers
    (doseq [worker (-> this tap :redis-consumer tap :consumers)]
      (tap (deref worker))
      (worker :stop))
    (dissoc this :redis-consumer))

  intf/CommonConsumer
  (listen
    [this settings] ;; settings will be {:consumer-x {:handler s/fn :error-callback s/fn :queue s/str}}
    (let [wcar-opts (-> this tap :redis-consumer :wcar)
          list (-> settings vals)
          consumers-list (start-list list wcar-opts)
          redis-consumer (tap (:redis-consumer this))
          with-consumers (assoc redis-consumer :consumers consumers-list)]
      (assoc this :redis-consumer with-consumers))))

(defn create-redis-consumer [server-info pool-settings]
  (->RedisConsumer server-info pool-settings))

(comment

  (zipmap (keys {:consumer-x {:handler println :error-callback println :queue "a-queue-x"}
                 :consumer-y {:handler println :error-callback println :queue "a-queue-y"}})
          (vals {:consumer-x {:handler println :error-callback println :queue "a-queue-x"}
                 :consumer-y {:handler println :error-callback println :queue "a-queue-y"}}))

  (vals {:consumer-x {:handler println :error-callback println :queue "a-queue-x"}
         :consumer-y {:handler println :error-callback println :queue "a-queue-y"}})

  (vals {:consumer-x {:handler println :error-callback println :queue "a-queue"}})

  (type (vals {:consumer-x {:handler println :error-callback println :queue "a-queue"}}))
  (type {:consumer-x {:handler println :error-callback println :queue "a-queue"}})

  (map println (vals {:consumer-x {:handler println :error-callback println :queue "a-queue-x"}
                      :consumer-y {:handler println :error-callback println :queue "a-queue-y"}}))

  ;;

  (merge default-producer-ops {})
  (merge default-producer-ops {:async? true}))