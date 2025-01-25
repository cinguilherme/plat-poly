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

(def     my-conn-spec {:uri "redis://localhost:6379/"})

(def     my-wcar-opts {:pool my-conn-pool, :spec my-conn-spec})
(def     my-conn-opts {:pool my-conn-pool :spec my-conn-spec})

;; (defmacro wcar* [& body] `(car/wcar my-wcar-opts ~@body))
(defn wcar* [opts & body] (car/wcar opts body))

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
  (send-message [this message]
    (let [wcar-opts (-> this :redis-producer :wcar)
          payload (-> message :message)
          destination (-> message :destination)]
      (car/wcar wcar-opts (car-mq/enqueue (:queue destination) payload)))))

(defn create-redis-producer [server-info pool-settings]
  (->RedisProducer server-info pool-settings))

(declare start-list)
(defn start-list [list wcar-opts]
  (mapv (fn [settings]
          (println "starting consumer for settings" settings)
         (let [_ (tap settings)
               queue (-> settings :queue)
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
      (assoc this :redis-consumer {:wcar wcar-opts}))
    (println "Starting Redis Consumer")
    this)
  (stop [this]
    (println "Stopping Redis Consumer")
    (dissoc this :redis-consumer))

  intf/CommonConsumer
  (listen
    [this settings] ;; settings will be {:consumer-x {:handler s/fn :error-callback s/fn :queue s/str}}
    (let [wcar-opts (-> this :redis-consumer :wcar)
          _ (tap (keys settings))
          list (tap (-> settings tap vals))]
      (start-list list wcar-opts))))

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
  )