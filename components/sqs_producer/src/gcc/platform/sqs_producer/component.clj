(ns gcc.platform.sqs_producer.component
  (:require [amazonica.core :as amazonica]
            [amazonica.aws.sqs :as sqs]
            [com.stuartsierra.component :as component]
            [schema.core :as s]
            [clojure.pprint :refer [pprint]])
  (:import (com.amazonaws.auth DefaultAWSCredentialsProviderChain)))

(defprotocol SQSProducer
  (create-queue [this queue-name attributes])
  (update-queue-attributes [this queue-name attributes])
  (send-message [this queue-name message]))

(defprotocol SQSProducerAsync
  (send-message-async [this queue-name message]))

(s/def Credentials
  {:access-key s/Str
   :secret-key s/Str
   :region s/Str
   :path-style-access s/Bool
   :endpoint s/Str})

(s/def SQSProducerConfig
  {:baseUrl (s/maybe s/Str)
   :queues (s/maybe [s/Any])
   :credentials (s/maybe Credentials)})

(defn- set-amazonica-credentials [credentials]
  (amazonica/defcredential credentials))

(defn- get-queue-url [baseUrl queue-name]
  (str baseUrl queue-name))

(s/defrecord SQSProducerComponent [config :- SQSProducerConfig]
  component/Lifecycle
  SQSProducer
  SQSProducerAsync

  (start [this]
    (println "Starting SQSProducerComponent")
    (set-amazonica-credentials (:credentials config))
    (assoc this :sqs-producer this 
           :credentials (:credentials config) 
           :queues (:queues config)
           :baseUrl (:baseUrl config)))

  (stop [this]
    (println "Stopping SQSProducerComponent")
    (assoc this :sqs-producer nil :credentials nil :queues nil :baseUrl nil))

  ;; Define a function to create a new SQSProducerComponent, only operate if sqs-producer is not nil
  (create-queue [this queue-name attributes]
    (when (:sqs-producer this)
      (let [queue-url
            (:queue-url (sqs/create-queue
                         {:queue-name queue-name
                          :attributes attributes}))]
        queue-url)))

  (update-queue-attributes [this queue-name attributes]
    (when (:sqs-producer this)
      (let [url (get-queue-url (:baseUrl this) queue-name)
            queue-url
            (:queue-url (sqs/set-queue-attributes
                         {:queue-url url
                          :attributes attributes}))]
        queue-url)))

  (send-message [this queue-name message]
    (when (:sqs-producer this)
      (let [url (get-queue-url (:baseUrl this) queue-name)
            reciept (sqs/send-message
                     {:queue-url url
                      :message-body message})]
        reciept)))

  ;;async version
  (send-message-async [this queue-name message]
    (when (:sqs-producer this)
      (let [url (get-queue-url (:baseUrl this) queue-name)
            freciept (future (sqs/send-message
                              {:queue-url url
                               :message-body message}))]
        freciept))))

(s/defn new-sqs-producer-component
  "Create a new SQSProducerComponent"
  [config :- SQSProducerConfig]
  (map->SQSProducerComponent {:config config}))

(comment


  (defn- create-standard-credentials []
    (DefaultAWSCredentialsProviderChain/getInstance))

  (def localstack-credentials
    {:access-key "test"
     :secret-key "test"
     :region "us-east-1"
     :path-style-access true
     :endpoint "http://localhost:4566"})

  (def config {:credential localstack-credentials
               :baseUrl "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/"
               :queues [{:name "qname-2" :properties {:queue-type "default"}}]})

  (amazonica/defcredential localstack-credentials)

  (def queue-url
    (:queue-url (sqs/create-queue
                 {:queue-name "qname-2"
                  :attributes
                  {:VisibilityTimeout 30 ; sec
                   :MaximumMessageSize 65536 ; bytes
                   :MessageRetentionPeriod 1209600 ; sec
                   :ReceiveMessageWaitTimeSeconds 10}})))

  (def component (component/start (new-sqs-producer-component config)))

  (component/stop component)

  (send-message component queue-url "Your message 33")

  (def ps (mapv (fn [v] (send-message-async component queue-url (str "Your message number " v))) (range 100)))

  (sqs/send-message {:queue-url queue-url
                     :message-body "Your message 2"})
  )