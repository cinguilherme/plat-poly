(ns gcc.platform.redis.core
  (:require [taoensso.carmine :as car :refer [wcar]]))

;; Define the Redis connection spec
(def redis-spec {:pool {}
                 :spec {:host "localhost"  ; Assuming Redis is running locally
                        :port 6379}})       ; Default Redis port

(defn set-key [key value]
  (wcar redis-spec (car/set key value)))

(defn get-key [key]
  (wcar redis-spec (car/get key)))

(comment

  (set-key "my-key" "Hello, Redis!")
  (get-key "my-key")  ; Should return "Hello, Redis!"


  )