(ns gcc.platform.mongo.core
   (:require [monger.core :as mg]
            [monger.collection :as mc]))

;; Initialize the MongoDB connection and return the connection and database {:conn conn :db db}
(defn init-mongo-connection 
  "Start a connetion to mongo DB return the connection and database {:conn conn :db db}"
  ([]
   (let [conn (mg/connect)
         db (mg/get-db conn "dev_db")]
     {:conn conn :db db}))
  ([server-address]
   (let [conn (mg/connect server-address)
         db (mg/get-db conn "dev_db")]
     {:conn conn :db db}))
  ([server-address db-name]
   (let [conn (mg/connect server-address)
         db (mg/get-db conn db-name)]
     {:conn conn :db db})))

(defn connect
  "Return oppened connection to MongoDB" 
  ([]
   (mg/connect))
  ([server-address]
   (mg/connect server-address)))

(defn disconnect
  "Close connection to MongoDB"
  [conn]
  (mg/disconnect conn))

(defn insert [db collection map-document]
  (mc/insert db collection map-document))

(defn create-collection [db collection ops]
  (mc/create db collection ops))


;; Example Usage
(comment

;; Create and insert a sample document
  (defn create-person [conn db name age]
    (mc/insert db "people" {:name name :age age}))

;; Query for a person by name
  (defn find-person [db name]
    (mc/find-maps db "people" {:name name}))

  (let [{:keys [conn db]} (init-mongo-connection)]
    (create-person conn db "John Doe" 30)
    (println (find-person db "John Doe"))  ; Should print the document for John Doe
    (mg/disconnect conn))  ; Disconnect when done

  (def conn (init-mongo-connection))

  (println conn)

  (println (find-person (:db conn) "John Doe"))

  (disconnect (:conn conn))
  )