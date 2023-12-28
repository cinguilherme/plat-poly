(ns gcc.platform.mongo.core
   (:require [monger.core :as mg]
            [monger.collection :as mc]))

;; Initialize the MongoDB connection
(defn init-mongo-connection []
  (let [conn (mg/connect)
        db (mg/get-db conn "your_database")]  ; Replace with your database name
    {:conn conn :db db}))

;; Create and insert a sample document
(defn create-person [conn db name age]
  (mc/insert db "people" {:name name :age age}))

;; Query for a person by name
(defn find-person [db name]
  (mc/find-maps db "people" {:name name}))

;; Example Usage
(comment
  (let [{:keys [conn db]} (init-mongo-connection)]
    (create-person conn db "John Doe" 30)
    (println (find-person db "John Doe"))  ; Should print the document for John Doe
    (mg/disconnect conn))  ; Disconnect when done
  
  (def conn (init-mongo-connection))

  (println conn)

  (println (find-person (:db conn) "John Doe"))

  (mg/disconnect (:conn conn))
)