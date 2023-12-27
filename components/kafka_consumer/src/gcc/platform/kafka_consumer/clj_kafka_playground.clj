(ns gcc.platform.kafka-consumer.clj-kafka-playground
  (:require [clj-kafka.core :as kafka :refer [with-resource]]
            [clj-kafka.zk :as zk]
            [clj-kafka.admin :as admin] 
            [clj-kafka.new.producer :as producer :refer [byte-array-serializer string-serializer record]] 
            [clj-kafka.consumer.simple :as kafka-consumer]
            [clj-kafka.consumer.zk :as consumer :refer [shutdown]]))

(comment

  (def kafka-config
    {"metadata.broker.list" "localhost:9092"
     "zookeeper.connect" "localhost:2181"
     "group.id" "clj-kafka.consumer"
     "serializer.class" "kafka.serializer.StringEncoder"
     "key.serializer.class" "kafka.serializer.StringEncoder"})

  (def consumer-config {"zookeeper.connect" "localhost:9092"
                        "group.id" "clj-kafka.consumer"
                        "auto.offset.reset" "smallest"
                        "auto.commit.enable" "false"})
  
  (zk/brokers {"zookeeper.connect" "localhost:2181"}) 
  
  (future (with-open [p (producer/producer {"bootstrap.servers" "localhost:9092"}
                                     (string-serializer)
                                     (string-serializer))]
            (let [res @(producer/send p (record "test-topic-3" "hello world new!!"))]
              (println res))
            (println "sent message"))) 

  (future (with-open [c (kafka-consumer/consumer "localhost" 9092 "clj-kafka.consumer")]
            (doseq [msg (take 40 (kafka-consumer/messages c "clj-kafka.consumer" "test-topic-3" 0 10 300000))]
              (println (String. (.value msg))))))


  (future (with-resource [c (consumer/consumer consumer-config)] 
            (println (take 2 (consumer/messages c "test-topic-3" )))))

  (def xform (comp (map println)))

  (future (with-resource [c (consumer/consumer consumer-config)]
            shutdown
            (let [stream (consumer/create-message-stream c "test-topic-3")]
              (run! println (eduction xform stream)))))

  (future (with-resource [c (consumer/consumer consumer-config)] 
            (let [messages (take 2 (consumer/messages c "test-topic-3"))]
              (println "Messages retrieved: " messages))))


  (with-open [zkz (admin/zk-client "localhost:2181")]
    (if-not (admin/topic-exists? zkz "test-topic-4")
      (admin/create-topic zkz "test-topic-4"
                          {:partitions 3
                           :replication-factor 1
                           :config {"cleanup.policy" "compact"}})))


  )
