(ns gcc.platform.dynamodb.interface
  (:require [gcc.platform.dynamodb.core :as core] 
            [schema.core :as s]))

(defprotocol DynamoDbAdmin
  (create-table [component table-name key-schema opts])
  (update-table [component table-name opts])
  (delete-table [component table-name])
  (list-tables [component]))

(defprotocol DynamoDBCore
  (get-item-by-id [component table id]) 
  (query [component table query])
  (put-item [component table item]))

(defn sample-by-id! [id]
  (core/get-by-id id))

(defn new-dynamo-component [client-opts]
  (component/new-dynamo-component client-opts))

(comment

  (sample-by-id! 1)

  )
