(ns gcc.platform.sqs_consumer.dev
  (:require [amazonica.core :as amazonica]
            [amazonica.aws.sqs :as sqs]
            [clojure.pprint :as pprint]) 
  (:import (com.amazonaws.auth DefaultAWSCredentialsProviderChain)))

(comment

  (defn create-standard-credentials []
    (DefaultAWSCredentialsProviderChain/getInstance))

  (def localstack-credentials
    {:access-key "test"
     :secret-key "test"
     :region "us-east-1"
     :path-style-access true
     :endpoint "http://localhost:4566"})

  ;; (amazonica/defcredential create-standard-credentials)

  (amazonica/defcredential localstack-credentials)

  (def queue-url
    (:queue-url (sqs/create-queue
                 {:queue-name "qname-2"
                  :attributes
                  {:VisibilityTimeout 30 ; sec
                   :MaximumMessageSize 65536 ; bytes
                   :MessageRetentionPeriod 1209600 ; sec
                   :ReceiveMessageWaitTimeSeconds 10}})))
  
  (pprint/pprint queue-url)

  (sqs/send-message {:queue-url queue-url
                     :message-body "Your message 2"})

  (defn consume-message []
    (let [messages (:messages (sqs/receive-message {:queue-url queue-url
                                                    :wait-time-seconds 5
                                                    :max-number-of-messages 10}))
          _ (println "Received messages:" messages)]
      (doseq [message messages]
        (println "Received message in consume:" (:body message))
        (sqs/delete-message {:queue-url queue-url
                             :receipt-handle (:receipt-handle message)}))))


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
                (pprint/pprint e))))
        ;; Continue the loop
          (recur)))))

  (pprint/pprint queue-url)

  (def continous (future (consume-messages-continuous queue-url println {} (atom true))))

  (future-cancel continous)

  (consume-message)
  )