(ns dev
  (:require [gcc.platform.files.interface :as files]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [com.stuartsierra.component :as component] 
            [gcc.platform.elastic_search.component :as esc]
            [gcc.platform.pedestal.component :as pedestal]
            [gcc.platform.redis.component :as rc]
            [gcc.platform.redis.interface :as redis-component]
            [cheshire.core :as json]
            [clojure.pprint :as pprint]))
            

(comment

  (defn respond-hello [request components-map]
  ;; Your implementation here, using components-map if needed
    {:status 200 :body "Hello, world!"})

  (defn respond-hello-json [request components-map]
  ;; Your implementation here, using components-map if needed
    (let [redis (get components-map :redis)
          elasticsearch (get components-map :elasticsearch)
          redis-value (redis-component/get-key redis "1")
          elasticsearch-value (esc/search-documents elasticsearch "my-index" {:title "Test"})]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/encode {:message "Hello, world! This is a JSON response. 🔥"
                           :redis-val redis-value
                           :elasticsearch elasticsearch-value})}))

  (def routes
    [["/greet" :get respond-hello :route-name :greet]
     ["/greet-json" :get respond-hello-json :route-name :greet-json]])

  (def component-map {:redis (component/start (rc/map->MockRedisComponent {})) 
                      :elasticsearch (component/start (esc/new-elasticsearch-component "http://localhost:9200/"))})

  (defn new-system []
    (component/system-map
     :elasticsearch (esc/new-elasticsearch-component "http://localhost:9200/")
     :redis (rc/new-redis-component "localhost" 6379)
     :pedestal (pedestal/new-pedestal-component routes component-map)
    ;; Add other components here
     ))

  (def system (component/start (new-system)))
  (component/stop system)

  (esc/index-document
   (:elasticsearch system)
   "my-index" "1" {:title "Test Document" :content "This is a test."})

  (esc/search-documents (:elasticsearch system) "my-index" {:title "Test"})

  (redis-component/get-key (:redis system) "1")
  (redis-component/set-key (:redis system) "1" "2")

  (println 1)

  )