(ns dev
  (:require [gcc.platform.files.interface :as files]
            [amazonica.core :as amazonica]
            [amazonica.aws.sqs :as sqs]
            [amazonica.aws.s3 :as s3]
            [clj-kafka.core :as kafka]
            [clj-kafka.admin :as admin]
            [clj-kafka.producer :as producer]
            [clj-kafka.new.producer :as new-producer :refer [byte-array-serializer record]]
            [clj-kafka.consumer.simple :as consumer]
            [clj-kafka.consumer.zk :as zk]) 
  (:import (com.amazonaws.auth DefaultAWSCredentialsProviderChain)))

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

(def kafka-config
  {"metadata.broker.list" "localhost:9092"
   "serializer.class" "kafka.serializer.StringEncoder"
   "key.serializer.class" "kafka.serializer.StringEncoder"})

(def config {"zookeeper.connect" "localhost:2182"
             "group.id" "clj-kafka.consumer"
             "auto.offset.reset" "smallest"
             "auto.commit.enable" "false"})

(admin/create-topic kafka-config "test")

(comment

  (with-open [p (new-producer/producer {"bootstrap.servers" "127.0.0.1:9092"} (byte-array-serializer) (byte-array-serializer))]
    (new-producer/send p (record "test-topic" (.getBytes "hello world!"))))

  (let [c (zk/consumer config)]
    zk/shutdown
    (take 2 (zk/messages c "test")))

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

  (s3/list-buckets)

  (s3/create-bucket {:bucket-name "another" :region "us-east-1"})


  (try
    (s3/create-bucket "my-bucket")
    (catch Exception e
      (println "Exception in creating bucket:" (.getMessage e))))


  (s3/put-object {:bucket-name "my-bucket"
                  :key "my-key"
                  :content-type "text/plain"
                  :content-length 11
                  :body "Hello World!"})


  )