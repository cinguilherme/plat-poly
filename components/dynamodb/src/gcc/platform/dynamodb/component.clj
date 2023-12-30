(ns gcc.platform.dynamodb.component
  (:require [taoensso.faraday :as far]
            [schema.core :as s]
            [com.stuartsierra.component :as component]))

(defprotocol DynamoDbAdmin
  (create-table [component table-name key-schema opts])
  (update-table [component table-name opts])
  (delete-table [component table-name])
  (list-tables [component]))

(defprotocol DynamoDBCore
  (get-item-by-id [component table id]) 
  (query [component table query])
  (put-item [component table item]))

(s/def DynamoClientOps 
  {:access-key s/Str
   :secret-key s/Str
   :endpoint s/Str})

(s/defrecord DynamoComponent [client-opts :- DynamoClientOps]
  component/Lifecycle
  DynamoDbAdmin
  DynamoDBCore

  (start [this]
    (assoc this :client-opts client-opts))
  (stop [this]
    (assoc this :client-opts nil))

  ;; Admin
  (list-tables [this]
    (far/list-tables (:client-opts this)))
  (create-table [this table-name key-schema opts]
    (far/create-table (:client-opts this) table-name key-schema opts))
  (update-table [this table-name opts]
    (far/update-table (:client-opts this) table-name opts))
  (delete-table [this table-name]
    (far/delete-table (:client-opts this) table-name))
  
  ;; Core
    
  )

(s/defn new-dynamo-component [client-opts :- DynamoClientOps]
  (map->DynamoComponent {:client-opts client-opts}))

(comment
  
  (def client-opts
  {:access-key "test"
   :secret-key "test"
   ;; port 4566 for LocalStack
   :endpoint "http://localhost:4566" 
   })
  
  (def dynamo-component (-> client-opts new-dynamo-component component/start))

  (list-tables dynamo-component)

  )

