(ns gcc.platform.pedestal-server.routes.migrations
  (:require [gcc.platform.elastic_search.component :as esc]
            [gcc.platform.redis.interface :as redis]
            [gcc.platform.postgres.interface :as postgres]
            [gcc.platform.dynamodb.interface :as dynamodb]

            ;;platform end
            [cheshire.core :as json]
            
            [clojure.pprint :refer [pprint]]))

(defn- random-model []
  (first (shuffle ["Civic" "Corolla" "Focus" "CLA290"])))

(defn- random-redis-models [size]
  (map (fn [v] {:id (random-uuid) :model (random-model)}) (range 1 (rand-int size))))

(defn- random-documents [size]
  (map (fn [v] 
         [ "my-index" 
          (str v)
          {:title "Test Document Sample"
           :content "This is a test on migration to have sample data."}]) 
       (range size)))

(defn- random-addresses [size]
  (map 
   (fn [v]
     ["insert into address (name, email) values (?, ?)"
      (str "John Doe" v) (str "john" v "@example.com")])
   (range size)))

;; return a random list of {:id uuid :model (one of the models from the list)}

(comment

  (future (mapv (fn [v]
                                 (pprint v)
                                 )
                               (random-addresses 5)))
  
  (mapv (fn [x]
                        (println (first x)
                                (second x)
                                (nth x 2)))
                       (random-documents 5))

  )

(defn migrations-volume [request components-map]
  (let [rc (get components-map :redis)
        ec (get components-map :elasticsearch)
        rs (get components-map :postgres)]
    (try
      (do
        (postgres/execute!
         rs
         ["
            create table if not exists address (id serial primary key,
            name varchar (32),
            email varchar (255))"])
        (let [fr (redis/set-key rc "100" (random-redis-models 30))
              fd (mapv (fn [x]
                         (esc/index-document
                          ec
                          (first x)
                          (second x)
                          (nth x 2)))
                       (random-documents 40))
              fp (mapv (fn [v]
                         (pprint v)
                         (postgres/execute!
                          rs
                          v))
                       (random-addresses 50))]
          (do
            (println fr)
            (println fd))))
      (catch Exception e
        (println "error in migrations massive")
        (println e)))
    {:status 200 :body "migration & data completed"}))

(defn migations-plus-sample-data [request components-map]
  (let [rc (get components-map :redis)
        ec (get components-map :elasticsearch)
        rs (get components-map :postgres)]
    (try 
      (do
        
        (redis/set-key rc "2" {:id (random-uuid) :model "Civic RSX"})
        (redis/set-key rc "3" [{:id (random-uuid) :model "Civic RSX"}
                               {:id (random-uuid) :model "Volvo C40"}])
        (esc/index-document
         ec
         "my-index" "5"
         {:title "Test Document Sample" :content "This is a test on migration to have sample data."})
        (postgres/execute!
         rs
         ["
            create table if not exists address (id serial primary key,
            name varchar (32),
            email varchar (255))"])
        (postgres/execute!
         rs
         ["
    insert into address (name, email) values (?, ?)"
          "John Doe Third" "john3@example.com"]))
      (catch Exception e
      (println "error in migrations")
      (println e)))
    {:status 200 :body "migration & data completed"}))