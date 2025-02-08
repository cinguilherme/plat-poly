(ns gcc.platform.common-messaging.rabbitmq.component
  (:require [com.stuartsierra.component :as component]
            [gcc.platform.common-messaging.protocols :as proto]
            [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.exchange  :as le]
            [langohr.queue     :as lq]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]
            [clojure.pprint :refer [pprint]]))

(defn tap [v]
  (pprint v)
  v)

(defrecord RabbitMQProducer [server-info connection channel]
  component/Lifecycle
  (start [this]
    (let [conn (rmq/connect server-info)
          ch (lch/open conn)]
      (println "[RabbitMQProducer] Connected to RabbitMQ.")
      (assoc this :connection conn :channel ch)))

  (stop [this]
    (when (:channel this) (rmq/close (:channel this)))
    (when (:connection this) (rmq/close (:connection this)))
    (println "[RabbitMQProducer] Disconnected from RabbitMQ.")
    (assoc this :connection nil :channel nil))

  proto/CommonProducer
  (send-message [this message ops]
    (let [exchange (:exchange (:destination message))  ;; Now using an exchange
          routing-key (:routing-key (:destination message)) ;; Routing key for queue binding
          payload (:message message)]
      (lb/publish (:channel this) exchange routing-key payload {:content-type "text/plain"})
      (println (str "[RabbitMQProducer] Sent message to exchange `" exchange "` with routing key `" routing-key "`"))))

  (send-messages [this messages ops]
    (doseq [msg messages]
      (proto/send-message this msg ops))))


(defrecord RabbitMQConsumer [server-info connection channel consumers]
  component/Lifecycle
  (start [this]
    (let [conn (rmq/connect server-info)
          ch (lch/open conn)
          consumers (atom {})]
      (println "[RabbitMQConsumer] Connected to RabbitMQ.")
      (assoc this :connection conn :channel ch :consumers consumers)))

  (stop [this]
    ;; Stop all consumers
    (doseq [[_ consumer] @(:consumers this)]
      (rmq/close consumer))
    (when (:channel this) (rmq/close (:channel this)))
    (when (:connection this) (rmq/close (:connection this)))
    (println "[RabbitMQConsumer] Disconnected from RabbitMQ.")
    (assoc this :connection nil :channel nil :consumers nil))

  proto/CommonConsumer
  (listen [this {:keys [exchange queue routing-key handler]}]
    (let [ch (:channel this)]
      ;; Declare exchange and queue
      (le/declare ch exchange "direct" {:durable true})
      (lq/declare ch queue {:exclusive false :auto-delete true})
      (lq/bind ch queue exchange {:routing-key routing-key})

      ;; Wrap handler to extract message payload
      (let [wrapped-handler (fn [ch meta ^bytes payload]
                              (let [message (String. payload "UTF-8")]
                                (handler message)))]

        (lc/subscribe ch queue wrapped-handler {:auto-ack true}))

      (println (str "[RabbitMQConsumer] Listening to queue `" queue "` bound to exchange `" exchange "` with routing key `" routing-key "`"))
      (swap! (:consumers this) assoc queue ch))))

;; Factory function to create a RabbitMQ consumer component
(defn create-rabbitmq-consumer [server-info]
  (map->RabbitMQConsumer {:server-info server-info}))

(defn create-rabbitmq-producer [server-info]
  (map->RabbitMQProducer {:server-info server-info}))
