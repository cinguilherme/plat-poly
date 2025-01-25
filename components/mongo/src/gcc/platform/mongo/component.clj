(ns gcc.platform.mongo.component
  (:require
   [com.stuartsierra.component :as component]
   [gcc.platform.common.protocols.doc-db :as doc-db]
   [gcc.platform.mongo.core :as core]))

(defn tap [v]
  (println v)
  v)

(defrecord MongoComponent [server-info db-name]
  component/Lifecycle
    (start [this]
      (let [vals (tap (doc-db/doc-connect this))]
        (assoc this :doc-db vals)))
    (stop [this]
      (let [{:keys [conn]} (tap (:doc-db this))]
        (doc-db/doc-disconnect this conn)
        (dissoc this :doc-db)))
  
  doc-db/Docdoc
  (doc-connect [this]
    (core/init-mongo-connection server-info db-name))
  (doc-disconnect [this conn]
    (core/disconnect conn))
  (doc-create-collection [this collection ops]
    (core/create-collection db-name collection ops))
  (doc-insert [this collection map-document]
    (core/insert collection map-document))
  (doc-update [this collection query update]
    (throw (Exception. "Not implemented")))
  (doc-delete [this collection query]
    (throw (Exception. "Not implemented")))
  (doc-find-maps [this collection query]
    (throw (Exception. "Not implemented")))
  (doc-find-map [this collection query]
    (throw (Exception. "Not implemented"))))

(defn mongo-component [server-info db-name]
    (->MongoComponent server-info db-name))