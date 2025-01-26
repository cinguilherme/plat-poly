(ns gcc.platform.pedestal-server.routes.hello-json
  (:require [cheshire.core :as json]
            [gcc.platform.elastic_search.interface :as esc]
            [gcc.platform.redis.interface :as redis]
            [gcc.platform.postgres.interface :as postgres]
            [gcc.platform.dynamodb.interface :as dynamodb]))


(defn postgres-stuff [postgres]
  (try
    (do
      (postgres/execute!
       postgres
       ["
            create table if not exists address (id serial primary key,
            name varchar (32),
            email varchar (255))"]))
    (catch Exception e
      (println "error in postgres-stuff create table")
      (println e)))
  (try
    (postgres/execute! postgres ["SELECT * FROM address"])
    (catch Exception e
      (println "error in postgres-stuff get address")
      (println e))))

(defn redis-stuff [redis]
  (try
    {:first (redis/get-key redis "1") 
     :second (redis/get-key redis "2")
     :third (redis/get-key redis "3")
     :100 (redis/get-key redis "100")}
    (catch Exception e
      (println "error in redis-stuff get-key")
      (println e))))

(defn elastic-search-stuff [elasticsearch]
  (try
    (esc/search-documents elasticsearch "my-index" {:title "Test"})
    (catch Exception e
      (println "error in elastic-search-stuff search-documents")
      (println e))))

(defn dynamodb-stuff [dynamodb]
  (try
    (dynamodb/list-tables dynamodb)
    (catch Exception e
      (println "error in dynamodb-stuff list-tables")
      (println e))))


(defn respond-hello-json [request components-map]
  ;; Your implementation here, using components-map if needed
  (let [redis (get components-map :redis)
        elasticsearch (get components-map :elasticsearch)
        postgres (get components-map :postgres) 
        dynamodb (get components-map :dynamodb)
          ;; needs try catch to avoid internal server error and actually see whats going on
        
        elasticsearch-value (future (elastic-search-stuff elasticsearch))
        redis-value (future (redis-stuff redis))
        postgres-value (future (postgres-stuff postgres))
        dynamodb-value (future (dynamodb-stuff dynamodb))]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/encode {:message "Hello, world! This is a JSON response. 🔥"
                         :redis-val @redis-value
                         :elasticsearch @elasticsearch-value
                         :postgres @postgres-value
                         :dynamodb @dynamodb-value})}))