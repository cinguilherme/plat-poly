(ns gcc.platform.sqs_producer.interface
  (:require [gcc.platform.sqs_producer.component :as component]))

(defn new-sqs-producer-component [credentials]
  (component/new-sqs-producer-component credentials))

(defn create-queue [sqs-producer queue-name attributes]
    (component/create-queue sqs-producer queue-name attributes))

(defn update-queue-attributes [sqs-producer queue-name attributes]
  
    (component/update-queue-attributes sqs-producer queue-name attributes))

(defn send-message [sqs-producer queue-name message]
    (component/send-message sqs-producer queue-name message))

(defn send-message-async [sqs-producer queue-name message]
  (component/send-message-async sqs-producer queue-name message))
