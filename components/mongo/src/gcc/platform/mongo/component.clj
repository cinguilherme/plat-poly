(ns gcc.platform.mongo.component
  (:require
   [com.stuartsierra.component :as component]
   [gcc.platform.common.interface :as c-i]
   [gcc.platform.mongo.core :as core]))

(defn tap [v]
  (println v)
  v)

(defrecord MongoComponent [server-info db-name]
  component/Lifecycle
    (start [this]
      (let [vals (tap (c-i/doc-connect this))]
        (assoc this :doc-db vals)))
    (stop [this]
      (let [{:keys [conn]} (tap (:doc-db this))]
        (c-i/doc-disconnect this conn)
        (dissoc this :doc-db)))
  
  common-interface/DocDB
  (doc-connect [this]
    (core/init-mongo-connection server-info db-name))
  (doc-disconnect [this conn]
    (core/disconnect conn))
  (doc-create-collection [this collection ops]
    (core/create-collection db-name collection ops))
  (doc-insert [this collection map-document] 
    (let [{:keys [conn db]} (tap (:doc-db this))]
      (core/insert db collection map-document)))
  (doc-update [this collection query update]
    (throw (Exception. "Not implemented")))
  (doc-delete [this collection query]
    (throw (Exception. "Not implemented")))
  (doc-find-maps [this collection query]
    (let [{:keys [conn db]} (tap (:doc-db this))]
      (core/find-maps db collection query))             )
  (doc-find-map [this collection query]
    (throw (Exception. "Not implemented"))))

(defn mongo-component [server-info db-name]
    (->MongoComponent server-info db-name))