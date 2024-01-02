(ns gcc.platform.sqs_consumer.interface
  (:require [gcc.platform.sqs_consumer.component :as component]))

(defn new-sqs-consumer-component [credentials]
  (component/new-sqs-consumer-component credentials))
