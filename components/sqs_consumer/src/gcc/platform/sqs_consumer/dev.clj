(ns gcc.platform.sqs_consumer.dev
  (:require [amazonica.core :as amazonica]
            [amazonica.aws.sqs :as sqs]) 
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

  (amazonica/defcredential localstack-credentials)

  (def queue-url
    (:queue-url (sqs/create-queue
                 {:queue-name "qname-2"
                  :attributes
                  {:VisibilityTimeout 30 ; sec
                   :MaximumMessageSize 65536 ; bytes
                   :MessageRetentionPeriod 1209600 ; sec
                   :ReceiveMessageWaitTimeSeconds 10}})))

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

  (consume-message)
  
  )