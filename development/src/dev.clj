(ns dev
  (:require [gcc.platform.files.interface :as files]
            [com.stuartsierra.component :as component] 
            [gcc.platform.elastic_search.component :as esc]
            [gcc.platform.redis.component :as rc]
            [clojure.pprint :as pprint]))

(comment

  (defn new-system []
    (component/system-map
     :elasticsearch (esc/new-elasticsearch-component "http://localhost:9200/")
     :redis (rc/new-redis-component "localhost" 6379)
    ;; Add other components here
     ))

  (def system (new-system))
  
  (component/start system)

  (esc/index-document
   (:elasticsearch system)
   "my-index" "1" {:title "Test Document" :content "This is a test."})

  (esc/search-documents (:elasticsearch system) "my-index" {:title "Test"})
  
  (rc/get-key (:redis system) "1")
  (rc/set-key (:redis system) "1" "2")

  (println 1)

  )