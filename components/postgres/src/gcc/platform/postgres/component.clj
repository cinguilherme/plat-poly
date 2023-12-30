(ns gcc.platform.postgres.component
  (:require [next.jdbc :as jdbc]
            [com.stuartsierra.component :as component] 
            [schema.core :as s]))


(defprotocol PostgresComponentCore 
  (execute! [component sql] "Executes a SQL statement without additional arguments")
  (execute-params! [component sql params] "Executes a SQL statement with additional parameters"))

;; Define the schema for Db configuration
(def DbConfigSchema
  {:dbtype s/Str
   :dbname s/Str
   :user s/Str
   :password s/Str
   :host s/Str
   :port s/Int})

(defrecord PostgresComponent [config]
  component/Lifecycle
  PostgresComponentCore

  (start [this]
    (println "Postgres component starting")
    (assoc this :datasource (jdbc/get-datasource config)))

  (stop [this]
    (println "Postgres component stopping")
    (assoc this :datasource nil))

  ;; PostgresComponentCore  

  (execute! [this sql]
    (let [ds (:datasource this)
          _ (println ds)]
      (jdbc/execute! ds sql)))

  (execute-params! [this sql params]
    (jdbc/execute! (:datasource this) sql params)))

;; Function to create a new PostgresComponent
(s/defn new-postgres-component [config :- DbConfigSchema]
  (map->PostgresComponent {:config config}))


(comment

  (def db {:dbtype "postgresql"
           :dbname "your_database"
           :user "your_user"
           :password "your_password"
           :host "localhost"
           :port 5432})

  (def h2-config {:dbtype "h2:mem"  ; Use in-memory H2 database
                  :dbname "test"    ; Name of the in-memory database
                  :user "sa"        ; Default user for H2
                  :password ""})    ; Default password for H2

  (def memory (component/start (new-postgres-component h2-config)))
  (def compo (component/start (new-postgres-component db)))

  (println memory)

  (:datasource compo)

  (execute! compo ["select * from address"])


  (execute! memory ["select * from address"])
  (execute!
   memory
   ["
    create table if not exists address (id serial primary key,
    name varchar (32),
    email varchar (255))"])
  (execute!
   memory
   ["
    insert into address (name, email) values (?, ?)"
    "John Doe Third" "john3@example.com"])
  
  
  
  (execute!
   compo
   ["
    insert into address (name, email) values (?, ?)"
    "John Doe Third" "john3@example.com"])


  (def ds (jdbc/get-datasource db))

  (println ds)

  (jdbc/execute!
   ds
   ["
    create table if not exists address (id serial primary key,
    name varchar (32),
    email varchar (255))"])

  (jdbc/execute! ds ["
                                insert into address (name, email) values (?, ?) "
                     " John Doe Second " " john2 @example.com "])


  (defn query-data []
    (jdbc/execute! ds [" select * from address "]))

  (query-data)
  )
