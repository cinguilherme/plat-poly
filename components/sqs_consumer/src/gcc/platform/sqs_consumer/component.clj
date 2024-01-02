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

(def Queues
  {:name (s/maybe s/Str)
   :properties (s/maybe s/Any)})

(def SQSConsumerConfig
  {:credentials {:access-key "test"
                 :secret-key "test"
                 :region "us-east-1"
                 :path-style-access true
                 :endpoint "http://localhost:4566"}
   :baseUrl (s/maybe s/Str)
   :queues [Queues]
   :queue-input (s/maybe QueuesConsumers)})

(def Handlers
  [{:queue (s/maybe s/Str)
    :handler (s/maybe s/Any)}])

(defn- queue-url-from-queue-name [queue-name config]
  (str (:baseUrl config) queue-name))

(defn- consume-messages-continuous [queue-url handler components-map running-flag]
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

(defn- with-queue-url [handlers configs]
  (mapv (fn [handler]
          (assoc handler :queue-url (queue-url-from-queue-name (:queue handler) configs)))
        handlers))

(defrecord SQSConsumerComponent [config handlers redis dynamodb postgres]
  component/Lifecycle

  (start [this] 
    (let [_ (amazonica/defcredential (:credentials config))
          queue-input (with-queue-url handlers config)
          components-map {:redis redis
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

(s/defn new-sqs-consumer-component [config :- SQSConsumerConfig handlers :- Handlers]
  (map->SQSConsumerComponent {:config config :handlers handlers}))

(comment

  (def localstack-credentials
    {:access-key "test"
     :secret-key "test"
     :region "us-east-1"
     :path-style-access true
     :endpoint "http://localhost:4566"})

  (amazonica/defcredential localstack-credentials)

  (def queue-url "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/qname-2")

  (def config {:credentials localstack-credentials
               :baseUrl "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/"
               :queues [{:name "qname-2" :properties {:queue-type "default"}}]})

  (def handlers [{:queue "qname-2"
                  :handler (fn [v components-map]
                             (println v)
                             (pprint components-map))}])
  
  (def consumers (component/start (new-sqs-consumer-component config handlers)))

  (def consumers (component/start (new-sqs-consumer-component [{:queue-url queue-url
                                                                :handler (fn [v components-map]
                                                                           (println v)
                                                                           (pprint components-map))}])))

  (component/stop consumers)
  (pprint consumers)

  )