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

(s/defn consume-messages [queue-url :- s/Str handler :- s/Any components-map :- ComponentsMap]
  ;; logic for consuming messages from the SQS queue
  )

(defrecord SQSConsumerComponent [queue-input consumers redis dynamodb postgres]
  component/Lifecycle

  (start [this]
    (let [components-map {:redis redis
                          :dynamodb dynamodb
                          :postgres postgres}
          consumers (mapv (fn [queue]
                            (future (consume-messages
                                     (:queue-url queue)
                                     (:handler queue)
                                     components-map)))
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
  
  (def consumers (component/start (new-sqs-consumer-component ["queue-url-1" "queue-url-2"])))

  (component/stop consumers)
  (pprint consumers)
  
  )