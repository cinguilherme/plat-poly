(ns gcc.platform.dynamodb.interface
  (:require [gcc.platform.dynamodb.core :as core]))

(defprotocol DynamoDbAdmin
  (create-table [table-name key-schema opts])
  (list-tables []))

(defprotocol DynamoDBCore
  (get-item-by-id [table id]) 
  (query [table query])
  (put-item [table item]))

(defn sample-by-id! [id]
  (core/get-by-id id))

(comment

  (sample-by-id! 1)

  )
