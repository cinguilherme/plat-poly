(ns gcc.platform.pedestal-server.core
  (:require [gcc.platform.pedestal.interface :as c.pedestal]
            [gcc.platform.pedestal.component :as pedestal]
            [gcc.platform.redis.component :as rc]
            [gcc.platform.redis.interface :as redis-component]
            [gcc.platform.postgres.component :as pg]
            [gcc.platform.postgres.interface :as postgres-component]
            [gcc.platform.dynamodb.component :as dynamodb]
            [gcc.platform.dynamodb.interface :as dynamodb-component]
            [gcc.platform.elastic_search.component :as esc]
            [com.stuartsierra.component :as component] 
            [gcc.platform.pedestal-server.routes.routes :as routes])
  (:gen-class))

(defn create-system []
  (component/system-map
   ;; leaf components (low level)
   :elasticsearch (esc/new-elasticsearch-component "http://localhost:9200/")

   :redis (rc/new-redis-component "localhost" 6379)
   
   :dynamodb (dynamodb/new-dynamo-component {:access-key "test"
                                             :secret-key "test"
                                             :endpoint "http://localhost:4566"})
    
   :postgres (pg/new-postgres-component {:dbtype "h2:mem"  ; Use in-memory H2 database
                                          :dbname "test"    ; Name of the in-memory database
                                          :user "sa"        ; Default user for H2
                                          :password ""})
   
   
   ;; compound components (high level)
   :pedestal (component/using
              (pedestal/new-pedestal-component routes/routes)
              [:redis :elasticsearch :dynamodb :postgres])
  ;; Add other components here
   ))

(defn -main [& args]
  (component/start (create-system)))

(defn shutdown [system]
  (component/stop system))

(comment

  (def system (-main "test"))

  (shutdown system)

  )