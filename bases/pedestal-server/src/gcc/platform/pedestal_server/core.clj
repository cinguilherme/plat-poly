(ns gcc.platform.pedestal-server.core
  (:require [gcc.platform.pedestal.interface :as pedestal]
            [gcc.platform.redis.interface :as redis]
            [gcc.platform.postgres.interface :as postgres]
            [gcc.platform.dynamodb.interface :as dynamodb]
            [gcc.platform.elastic_search.component :as esc]
            [com.stuartsierra.component :as component]
            [gcc.platform.common.interface :as common]
            [gcc.platform.envs.interface :as envs]
            [gcc.platform.pedestal-server.routes.routes :as routes] 
            [clojure.pprint :refer [pprint]])
  (:gen-class))

(def config (envs/load-config-for-env-plus-envs 
             [:DYNAMO_ENDPOINT :REDIS_HOST :REDIS_PORT]))

(pprint config)

(def elasticsearch-endpoint (-> config :config :elasticsearch :endpoint))
(def redis-conf (-> config :config :redis))
(def dynamo-conf (-> config :config :dynamodb))
(def relational-conf (-> config :config :relational))

(pprint relational-conf)

(defn create-system []
  (pprint config)
  (component/system-map
   ;; leaf components (low level)
   
   ;; Elasticsearch
   :elasticsearch (esc/new-elasticsearch-component elasticsearch-endpoint)

   ;; Redis
   :redis (redis/create-new-redis-component (:host redis-conf) (:port redis-conf))

   ;; DynamoDB
   :dynamodb (dynamodb/create-component dynamo-conf)

   ;; Postgres
   :postgres (postgres/new-postgres-component relational-conf)

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