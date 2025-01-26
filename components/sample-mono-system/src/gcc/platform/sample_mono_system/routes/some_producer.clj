(ns gcc.platform.sample-mono-system.routes.some-producer
  (:require [cheshire.core :as json]
            [gcc.platform.sqs_producer.interface :as sqs-producer]))

(defn hello-producer [request components-map] 
  (let [sq (get components-map :sqs-producer)
        queue (sqs-producer/create-queue
               sq
               "qname-2"
               {:VisibilityTimeout 30 ; sec
                :MaximumMessageSize 65536 ; bytes
                :MessageRetentionPeriod 1209600 ; sec
                :ReceiveMessageWaitTimeSeconds 10})
        produced (sqs-producer/send-message sq "qname-2" "hello world")] 
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/encode {:message "Hello, world! This is a JSON response. 🔥"
                         :produced produced})}))