(ns gcc.platform.sample-mono-system.interface
  (:require [com.stuartsierra.component :as component]
            [clojure.pprint :refer [pprint]]
            
            ;; interfaces
            [gcc.platform.pedestal.interface :as pedestal]
            [gcc.platform.sqs_producer.interface :as sqs-producer]
            [gcc.platform.redis.interface :as redis]
            [gcc.platform.postgres.interface :as postgres]
            [gcc.platform.dynamodb.interface :as dynamodb]
            [gcc.platform.elastic_search.interface :as esc]
            [gcc.platform.envs.interface :as envs]

            ;; some other components
            [gcc.platform.common-messaging.interface :as common-messaging]
            [gcc.platform.common-messaging.interface :as proto-messaging]

            ;; self namespaces
            [gcc.platform.sample-mono-system.core :as core]
            [gcc.platform.sample-mono-system.routes.routes :as routes]))


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

(def localstack-credentials
  {:access-key "test"
   :secret-key "test"
   :region "us-east-1"
   :path-style-access true
   :endpoint "http://localhost:4566"})

(def config {:credentials localstack-credentials
             :baseUrl "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/"
             :queues [{:name "qname-2" :properties {:queue-type "default"}}]})

(def events-map
  {:queue-1 "a-queue"
   :queue-2 "a-queue-2"})

(def bus (atom {}))

;;messaging producer & consumer config map
(def messaging-configs
  {:bus bus
   :events-map events-map
   :consumer-map {:first {:queue "a-queue"
                          :handler (fn [message]
                                     (println "Received" message))}
                  :second {:queue "a-queue-2"
                           :handler (fn [message]
                                      (println "Received" message))}}})

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
              [:redis :elasticsearch :dynamodb :postgres :sqs-producer :producer])

   :producer (common-messaging/new-producer-component {:kind :in-mem 
                                                       :configs messaging-configs})

   :consumer (common-messaging/new-consumer-component {:kind :in-mem
                                                       :configs messaging-configs})
   ;; GraphQL
   

   ;;:sqs-consumer (sqs-consumer/new-sqs-consumer-component config handlers)
   
   ;; Add other components here
   ))


(comment
  ;;

  (def system-map (create-system))

  system-map

  (def up-system (component/start system-map))
  up-system
  (def down-system (component/stop up-system))

  (defn -main [& args]
    (component/start (create-system)))

  (defn shutdown [system]
    (component/stop system))

  (def system (-main "test"))

  (shutdown system)

  (println "end of comment")

  ;;
  )