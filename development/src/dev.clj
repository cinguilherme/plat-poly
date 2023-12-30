(ns dev
  (:require [gcc.platform.files.interface :as files]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [com.stuartsierra.component :as component] 
            [gcc.platform.elastic_search.component :as esc]
            [gcc.platform.pedestal.component :as pedestal]
            [gcc.platform.redis.component :as rc]
            [gcc.platform.redis.interface :as redis-component]
            [clojure.pprint :as pprint]))
            

(comment

  (defn respond-hello [request]
    {:status 200 :body "Hello, world!"})
  
  (def routes
    (route/expand-routes
     #{["/greet" :get respond-hello :route-name :greet]}))

  (defn new-system []
    (component/system-map
     :elasticsearch (esc/new-elasticsearch-component "http://localhost:9200/")
     :redis (rc/new-mock-redis-component)
     :pedestal (pedestal/new-pedestal-component routes)
    ;; Add other components here
     ))

  (def system (new-system))
  (component/stop system)
  (component/start system)

  (esc/index-document
   (:elasticsearch system)
   "my-index" "1" {:title "Test Document" :content "This is a test."})

  (esc/search-documents (:elasticsearch system) "my-index" {:title "Test"})
  
  (redis-component/get-key (:redis system) "1")
  (redis-component/set-key (:redis system) "1" "2")

  (println 1)

  )