(ns gcc.platform.pedestal-server.routes.routes
  (:require [gcc.platform.elastic_search.component :as esc]
            [gcc.platform.redis.interface :as redis]
            [gcc.platform.postgres.interface :as postgres]
            [gcc.platform.dynamodb.interface :as dynamodb]

            ;;platform end
            [cheshire.core :as json]
            [gcc.platform.pedestal-server.routes.migrations :as migrations]
            [gcc.platform.pedestal-server.routes.hello-json :as hello]))

(defn respond-hello [request components-map]
  ;; Your implementation here, using components-map if needed
  {:status 200 :body "Hello, world!"})



(def routes
  [["/greet" :get respond-hello :route-name :greet]
   ["/migrations" :post migrations/migations-plus-sample-data :route-name :migrations]
   ["/migrations-2" :post migrations/migrations-volume :route-name :migrations-2]
   ["/greet-json" :get hello/respond-hello-json :route-name :greet-json]])