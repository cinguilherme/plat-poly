(ns gcc.platform.sqs_producer.component
  (:require [amazonica.core :as amazonica]
            [amazonica.aws.sqs :as sqs]
            [com.stuartsierra.component :as component]
            [schema.core :as s]
            [clojure.pprint :refer [pprint]])
  (:import (com.amazonaws.auth DefaultAWSCredentialsProviderChain)))

(defprotocol SQSProducer
  (create-queue [this queue-name attributes])
  (update-queue-attributes [this url attributes])
  (send-message [this url message]))

(defprotocol SQSProducerAsync
  (send-message-async [this url message]))

(s/def Credentials
  {:access-key s/Str
   :secret-key s/Str
   :region s/Str
   :path-style-access s/Bool
   :endpoint s/Str})

(defn- set-amazonica-credentials [credentials]
  (amazonica/defcredential credentials))

(s/defrecord SQSProducerComponent [credentials :- Credentials]
  component/Lifecycle
  SQSProducer
  SQSProducerAsync

  (start [this]
    (set-amazonica-credentials credentials)
    (assoc this :sqs-producer this :credentials credentials))

  (stop [this]
    (assoc this :sqs-producer nil :credentials nil))

  ;; Define a function to create a new SQSProducerComponent, only operate if sqs-producer is not nil
  (create-queue [this queue-name attributes]
    (when (:sqs-producer this)
      (let [queue-url
            (:queue-url (sqs/create-queue
                         {:queue-name queue-name
                          :attributes attributes}))]
        queue-url)))

  (update-queue-attributes [this url attributes]
    (when (:sqs-producer this)
      (let [queue-url
            (:queue-url (sqs/set-queue-attributes
                         {:queue-url url
                          :attributes attributes}))]
        queue-url)))

  (send-message [this url message]
    (when (:sqs-producer this)
      (let [reciept (sqs/send-message
                     {:queue-url url
                      :message-body message})]
        reciept)))

  ;;async version
  (send-message-async [this url message]
    (when (:sqs-producer this)
      (let [freciept (future (sqs/send-message
                             {:queue-url url
                              :message-body message}))]
        freciept))))

(defn new-sqs-producer-component
  "Create a new SQSProducerComponent"
  [credentials]
  (SQSProducerComponent. credentials))

(comment


  (defn- create-standard-credentials []
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

  (def component (component/start (new-sqs-producer-component localstack-credentials)))

  (component/stop component)

  (send-message component queue-url "Your message 33")
  
  (def ps (mapv (fn [v] (send-message-async component queue-url (str "Your message number " v))) (range 100)))
  
  (sqs/send-message {:queue-url queue-url
                     :message-body "Your message 2"})
  )