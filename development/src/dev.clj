(ns dev
  (:require [com.stuartsierra.component :as component]

            ;; platform start
            [gcc.platform.elastic_search.interface :as es]
            [gcc.platform.pedestal.component :as pedestal]
            [gcc.platform.redis.component :as rc]
            [gcc.platform.redis.interface :as redis-component]
            [gcc.platform.postgres.component :as pg]
            [gcc.platform.postgres.interface :as postgres-component]
            [gcc.platform.dynamodb.component :as dynamodb]
            [gcc.platform.dynamodb.interface :as dynamodb-component]

            ;;platform end 
            [cheshire.core :as json]))

            
(comment

  (defn respond-hello [request components-map]
  ;; Your implementation here, using components-map if needed
    {:status 200 :body "Hello, world!"})

  (defn respond-hello-json [request components-map]
  ;; Your implementation here, using components-map if needed
    (let [redis (get components-map :redis)
          elasticsearch (get components-map :elasticsearch)
          postgres (get components-map :postgres)
          dynamodb (get components-map :dynamodb)
          redis-value (future (redis-component/get-key redis "1"))
          elasticsearch-value (future (esc/search-documents elasticsearch "my-index" {:title "Test"}))
          
          _ (postgres-component/execute!
             postgres
             ["
    create table if not exists address (id serial primary key,
    name varchar (32),
    email varchar (255))"])
          
    ;;       __ (postgres-component/execute!
    ;;          postgres
    ;;          ["
    ;; insert into address (name, email) values (?, ?)"
    ;;           "John Doe Third" ""])
          
          postgres-value (future (postgres-component/execute! postgres ["SELECT * FROM address"]))
          
          dynamodb-value (future (dynamodb-component/list-tables dynamodb))]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/encode {:message "Hello, world! This is a JSON response. 🔥"
                           :redis-val @redis-value
                           :elasticsearch @elasticsearch-value
                           :postgres @postgres-value
                           :dynamodb @dynamodb-value})}))

  (def routes
    [["/greet" :get respond-hello :route-name :greet]
     ["/greet-json" :get respond-hello-json :route-name :greet-json]])

  (defn new-system []
    (component/system-map
     ;; leaf components (low level)
     :elasticsearch (es/new-elasticsearch-component "http://localhost:9200/")

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
                (pedestal/new-pedestal-component routes)
                [:redis :elasticsearch :dynamodb :postgres])
    ;; Add other components here
     ))

  (def system (component/start (new-system)))
  (component/stop system)
  )