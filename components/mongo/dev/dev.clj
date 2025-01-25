(ns dev
  (:require [gcc.platform.common.core :as common]
            [gcc.platform.mongo.core :as core]
            [com.stuartsierra.component :as component]
            [gcc.platform.mongo.component :as doc-db]))

(comment
  ;; 

  (def conf (doc-db/mongo-component "localhost" "dev_db"))
  (def active-component (component/start conf))

  (component/stop active-component)

  (def system
    (component/system-map
     :mongo (doc-db/mongo-component "localhost" "dev_db")))

  (def active-system (component/start system))
  (component/stop-system active-system)

  (def mongo-active-component (:mongo active-system))

  

  (def conn (core/init-mongo-connection))

  (println conn)

  (core/disconnect (:conn conn))

  ;;
  )