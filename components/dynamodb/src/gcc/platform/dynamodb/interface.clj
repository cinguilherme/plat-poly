(ns gcc.platform.dynamodb.interface
  (:require [gcc.platform.dynamodb.component :as component]))


(defn new-dynamo-component [client-opts]
  (component/new-dynamo-component client-opts))

(defn create-component [client-opts]
  (new-dynamo-component client-opts))

(defn list-tables [component]
  (component/list-tables component))

(comment

  

  )
