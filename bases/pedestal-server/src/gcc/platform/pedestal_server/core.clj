(ns gcc.platform.pedestal-server.core
  (:require [gcc.platform.pedestal.interface :as pedestal]
            [gcc.platform.sqs_producer.interface :as sqs-producer]
            [gcc.platform.sqs_consumer.interface :as sqs-consumer]
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
(def sqs-producer-conf (-> config :config :producer :sqs))

(pprint sqs-producer-conf)
(pprint relational-conf)

(def credentials
  {:access-key (envs/get-env :AWS_ACCESS_KEY_ID)
   :secret-key (envs/get-env :AWS_SECRET_ACCESS_KEY)
   :region (envs/get-env :AWS_REGION)
   :path-style-access false
   :endpoint (envs/get-env :AWS_ENDPOINT)})

(def localstack-credentials
    {:access-key "test"
     :secret-key "test"
     :region "us-east-1"
     :path-style-access true
     :endpoint "http://localhost:4566"})

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

   ;; SQS Producer
   :sqs-producer (sqs-producer/new-sqs-producer-component sqs-producer-conf)

   ;; compound components (high level)
   :pedestal (component/using
              (pedestal/new-pedestal-component routes/routes)
              [:redis :elasticsearch :dynamodb :postgres :sqs-producer])
   
   :sqs-consumer (sqs-consumer/new-sqs-consumer )

   ;; Add other components here
   ))

(defn -main [& args]
  (component/start (create-system)))

(defn shutdown [system]
  (component/stop system))

(comment

  (def system (-main "test"))

  (shutdown system)
  
  (println "end of comment")
  )