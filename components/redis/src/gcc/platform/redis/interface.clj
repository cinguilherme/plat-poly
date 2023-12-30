(ns gcc.platform.redis.interface
  (:require [gcc.platform.redis.component :as component]))

(defn create-new-redis-component [host port]
  (component/new-redis-component host port))

(defn get-key [component key]
  (component/get-key component key))

(defn set-key [component key value]
  (component/set-key component key value))

