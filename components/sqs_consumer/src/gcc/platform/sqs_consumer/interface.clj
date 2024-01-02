(ns gcc.platform.sqs_consumer.interface
  (:require [gcc.platform.sqs-consumer.component :as component]))

(defn new-sqs-consumer-component [config handlers]
  (component/new-sqs-consumer-component config handlers))
