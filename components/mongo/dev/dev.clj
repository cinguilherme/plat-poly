(ns dev
  (:require [gcc.platform.mongo.core :as core]))

(comment
  ;; 
  
  (def conn (core/init-mongo-connection))

  (println conn) 

  (core/disconnect (:conn conn))
  
  ;;
  )