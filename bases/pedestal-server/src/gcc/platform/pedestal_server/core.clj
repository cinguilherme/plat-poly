(ns gcc.platform.pedestal-server.core
  (:require [gcc.platform.pedestal.interface :as pedestal]
            [gcc.platform.redis.interface :as redis]
            [gcc.platform.postgres.interface :as postgres]
            [gcc.platform.dynamodb.interface :as dynamodb]
            [gcc.platform.elastic_search.component :as esc]
            [com.stuartsierra.component :as component]
            [gcc.platform.common.interface :as common]
            [gcc.platform.pedestal-server.routes.routes :as routes]
            [clojure.edn :as edn]
            [dotenv :refer [env]]
            [clojure.java.io :as io]
            [cprop.core :refer [load-config]]
            [clojure.pprint :refer [pprint]])
  (:gen-class))

(def config (common/load-config-for-env))

(pprint config)

(defn create-system []
  (component/system-map
   ;; leaf components (low level)
   
   ;; Elasticsearch
   :elasticsearch (esc/new-elasticsearch-component "http://localhost:9200/")

   ;; Redis
   :redis (redis/create-new-redis-component "localhost" 6379)

   ;; DynamoDB
   :dynamodb (dynamodb/create-component {:access-key "test"
                                         :secret-key "test"
                                         :endpoint "http://localhost:4566"})

   ;; Postgres
   :postgres (postgres/new-postgres-component {:dbtype "h2:mem"  ; Use in-memory H2 database
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