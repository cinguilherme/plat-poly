(ns gcc.platform.common-messaging.nats.component
  (:require [com.stuartsierra.component :as component]
            [gcc.platform.common-messaging.nats.core :as core]
            [nats.stream :as stream]
            [gcc.platform.common-messaging.protocols :as proto]
            [nats.core :as nats]
            [nats.consumer :as consumer]))

;;end

;; producer
(defrecord NATSProducer [server-info config connection]
  component/Lifecycle
  (start [this]
    (let [conn (nats/connect (:url server-info))
          queues (:queues config)]
      (println "[NATSProducer] Connected to NATS.")
      ;; Ensure streams exist based on config
      (doseq [queue queues]
        (core/ensure-stream-exists conn queue))
      (assoc this :connection conn)))

  (stop [this]
    (when (:connection this) (nats/close (:connection this)))
    (println "[NATSProducer] Disconnected from NATS.")
    (assoc this :connection nil))

  proto/CommonProducer
  (send-message [this {:keys [destination message]} opts]
    (let [conn (:connection this)
          subject (:queue destination)]
      (stream/publish conn
                      {:nats.message/subject subject
                       :nats.message/data message})
      (println (str "[NATSProducer] Sent message to `" subject "`"))))

  (send-messages [this messages opts]
    (doseq [msg messages]
      (proto/send-message this msg opts))))

;; consumer
(defrecord NATSConsumer [server-info config connection consumer-name queue handler thread]
  component/Lifecycle
  (start [this]
    (let [conn (nats/connect (:url server-info))
          safe-name (or consumer-name (str "worker-" (core/sanitize-consumer-name queue)))
          stream (core/find-stream-for-queue config queue)]
      (if-not stream
        (throw (ex-info (str "[NATSConsumer] Queue `" queue "` not found in config.") {:queue queue :config config}))
        (do
          (println (str "[NATSConsumer] Connected to NATS. Listening on queue `" queue "` in stream `" stream "` with consumer `" safe-name "`"))
          (consumer/create-consumer conn
                                    {:nats.consumer/stream-name stream
                                     :nats.consumer/name safe-name
                                     :nats.consumer/durable? true
                                     :nats.consumer/filter-subjects [queue]})
          ;; Start message processing loop
          (let [listener-thread
                (doto (Thread.
                       (fn []
                         (with-open [subscription (consumer/subscribe conn stream safe-name)]
                           (while true
                             (when-let [msg (consumer/pull-message subscription 1000)]
                               (consumer/ack conn msg)
                               (handler (:nats.message/data msg))
                               (println (str "[NATSConsumer] Processed message from `" queue "`")))))))
                  (.start))]
            (assoc this :connection conn :thread listener-thread))))))

  (stop [this]
    (when (:connection this) (nats/close (:connection this)))
    (when thread (.interrupt thread))
    (println "[NATSConsumer] Disconnected from NATS.")
    (assoc this :connection nil :thread nil))

  proto/CommonConsumer
  (listen [this {:keys [queue handler]}]
    (assoc this :queue queue :handler handler)))