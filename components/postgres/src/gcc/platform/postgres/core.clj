(ns gcc.platform.postgres.core
  (:require [next.jdbc :as jdbc]))

(comment
  
  (def db {:dbtype "postgresql"
         :dbname "your_database"
         :user "your_user"
         :password "your_password"
         :host "localhost"
         :port 5432})


  (def ds (jdbc/get-datasource db))

  (println ds)

  (jdbc/execute! ds ["
   create table if not exists address (
   id serial primary key,
   name varchar(32),
   email varchar(255)
   )"])
  
  (jdbc/execute! ds ["
   insert into address (name, email) values (?, ?)"
   "John Doe Second" "john2@example.com"])
  

  (defn query-data []
  (jdbc/execute! ds ["select * from address"]))

  (query-data)
  )
