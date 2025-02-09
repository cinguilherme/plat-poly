(ns gcc.platform.common-messaging.nats.dev
  (:require [nats.core :as nats]
            [nats.message :as nats.message]
            [nats.consumer :as consumer]
            [nats.stream :as stream]
            [com.stuartsierra.component :as component]
            [gcc.platform.common-messaging.protocols :as proto]
            [gcc.platform.common-messaging.nats.component :as nats-component]))


(def conn (nats/connect "nats://localhost:4222"))

(def config-map {:queues [{:name "task-stream"
                           :subjects ["tasks.>"]
                           :retention-policy :work-queue}
                          {:name "task-stream-2"
                           :subjects ["tasks2.>"]
                           :retention-policy :work-queue}]})

(def my-stream
  (stream/create-stream conn
                        {:nats.stream/name "task-stream"
                         :nats.stream/subjects ["tasks.>"]
                         :nats.stream/retention-policy :nats.retention-policy/work-queue}))

(stream/publish conn
                {:nats.message/subject "tasks.email"
                 :nats.message/data {:to "user@example.com"
                                     :subject "Welcome!"}})

(stream/publish conn
                {:nats.message/subject "tasks.sms"
                 :nats.message/data {:to "+1234567890"
                                     :text "Hello via SMS!"}})

(consumer/create-consumer conn
                          {:nats.consumer/stream-name "task-stream"
                           :nats.consumer/name "worker-1"
                           :nats.consumer/durable? true
                           :nats.consumer/filter-subjects ["tasks.email"]})

(consumer/create-consumer conn
                          {:nats.consumer/stream-name "task-stream"
                           :nats.consumer/name "worker-2"
                           :nats.consumer/durable? true
                           :nats.consumer/filter-subjects ["tasks.sms"]})

(defn listen-loop [cn consumer-name]
  (with-open [subscription (consumer/subscribe cn "task-stream" consumer-name)]
    (while true
      (when-let [msg (consumer/pull-message subscription 1000)]
        (consumer/ack cn msg)
        (prn (str consumer-name " processed message:") msg)))))

(def worker-thread-1
  (doto (Thread. (partial listen-loop conn "worker-1"))
    (.start)))

(def worker-thread-2
  (doto (Thread. (partial listen-loop conn "worker-2"))
    (.start)))



(def config-map
  {:queues [{:name "task-stream"
             :subjects ["tasks.>"]
             :retention-policy :work-queue}
            {:name "task-stream-2"
             :subjects ["tasks2.>"]
             :retention-policy :work-queue}]})


(def producer
  (nats-component/map->NATSProducer
   {:server-info {:url "nats://localhost:4222"}
    :config config-map}))

(def started-producer (component/start producer))
(proto/send-message started-producer {:destination {:queue "tasks.email"}
                                      :message {:to "user@example.com"
                                                :subject "Welcome!"}})

(def consumer (nats-component/map->NATSConsumer {:server-info {:url "nats://localhost:4222"}
                                  :config config-map
                                  :queue "tasks.email"}))

(def started-consumer (component/start consumer))