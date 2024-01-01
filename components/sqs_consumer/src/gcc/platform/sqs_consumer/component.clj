(ns gcc.platform.sqs-consumer.component
  (:require [amazonica.core :as amazonica]
            [amazonica.aws.sqs :as sqs]
            [com.stuartsierra.component :as component]
            [schema.core :as s]
            [clojure.pprint :refer [pprint]]) 
  (:import (com.amazonaws.auth DefaultAWSCredentialsProviderChain)))

(def ComponentsMap
  {:dynamodb (s/maybe s/Any)
   :postgres (s/maybe s/Any)
   :redis (s/maybe s/Any)})

(def QueuesConsumers
  [{:queue-url (s/maybe s/Str)
    :handler (s/maybe s/Any)}])

(defn consume-messages-continuous [queue-url handler components-map running-flag]
    (loop []
      (when @running-flag
        (let [messages (:messages (sqs/receive-message {:queue-url queue-url
                                                        :wait-time-seconds 5
                                                        :max-number-of-messages 10}))]
          (doseq [message messages]
            (try
            ;; Call the handler with the message and components-map
              (handler (:body message) components-map)
              (sqs/delete-message {:queue-url queue-url
                                   :receipt-handle (:receipt-handle message)})
              (catch Exception e
              ;; Handle exceptions here, possibly logging the error
                (pprint e)
                )))
        ;; Continue the loop
          (recur)))))

(defrecord SQSConsumerComponent [queue-input consumers redis dynamodb postgres]
  component/Lifecycle

  (start [this]
    (let [components-map {:redis redis
                          :dynamodb dynamodb
                          :postgres postgres}
          consumers (mapv (fn [queue]
                            (future (consume-messages-continuous
                                     (:queue-url queue)
                                     (:handler queue)
                                     components-map
                                     (atom true))))
                          queue-input)]
      (assoc this :consumers consumers)))

  (stop [this]
    ;; logic to gracefully stop the consumers
    (doseq [consumer (:consumers this)]
      (future-cancel consumer))
    (assoc this :consumers nil)))


;; Define a function to create a new SQSConsumerComponent
(s/defn new-sqs-consumer-component [queue-input :- QueuesConsumers]
  (map->SQSConsumerComponent {:queue-input queue-input :consumers []}))

(comment 
  
  (def queue-url "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/qname-2")

  (def consumers (component/start (new-sqs-consumer-component [{:queue-url queue-url  
                                                                :handler (fn [v components-map]
                                                                           (println v)
                                                                           (pprint components-map))}])))

  (component/stop consumers)
  (pprint consumers)
  
  )