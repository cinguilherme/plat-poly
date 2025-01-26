(ns gcc.platform.sample-mono-system.routes.routes
  (:require [gcc.platform.elastic_search.component :as esc]
            [gcc.platform.redis.interface :as redis]
            [gcc.platform.postgres.interface :as postgres]
            [gcc.platform.dynamodb.interface :as dynamodb]

            ;;platform end
            [cheshire.core :as json]
            
            ;;
            [gcc.platform.sample-mono-system.routes.migrations :as migrations]
            [gcc.platform.sample-mono-system.routes.hello-json :as hello]
            [gcc.platform.sample-mono-system.routes.some-producer :as some-producer]))

(defn respond-hello [request components-map]
  ;; Your implementation here, using components-map if needed
  {:status 200 :body "Hello, world!"})

(def routes
  [["/greet" :get respond-hello :route-name :greet]
   ["/migrations" :post migrations/migations-plus-sample-data :route-name :migrations]
   ["/migrations-2" :post migrations/migrations-volume :route-name :migrations-2]
   ["/producer" :post some-producer/hello-producer :route-name :producer]
   ["/greet-json" :get hello/respond-hello-json :route-name :greet-json]])