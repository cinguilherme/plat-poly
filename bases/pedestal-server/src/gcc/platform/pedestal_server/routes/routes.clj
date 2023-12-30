(ns gcc.platform.pedestal-server.routes.routes
  (:require [gcc.platform.elastic_search.component :as esc]
            [gcc.platform.redis.interface :as redis]
            [gcc.platform.postgres.interface :as postgres]
            [gcc.platform.dynamodb.interface :as dynamodb]
            
            ;;platform end
            [cheshire.core :as json]))


  (defn respond-hello [request components-map]
  ;; Your implementation here, using components-map if needed
    {:status 200 :body "Hello, world!"})

  (defn respond-hello-json [request components-map]
  ;; Your implementation here, using components-map if needed
    (let [redis (get components-map :redis)
          elasticsearch (get components-map :elasticsearch)
          postgres (get components-map :postgres)
          dynamodb (get components-map :dynamodb)
          redis-value (future (redis/get-key redis "1"))
          elasticsearch-value (future (esc/search-documents elasticsearch "my-index" {:title "Test"}))
          
          _ (postgres/execute!
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
          
          postgres-value (future (postgres/execute! postgres ["SELECT * FROM address"]))
          
          dynamodb-value (future (dynamodb/list-tables dynamodb))]
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