(ns gcc.platform.redis.component-test
 (:require [clojure.test :as test :refer :all]
           [gcc.platform.redis.component :as redis]
           [com.stuartsierra.component :as component]
           [gcc.platform.redis.interface :as redis-component]))


(def test-redis-component
  (redis/new-mock-redis-component))

(component/start test-redis-component)

(deftest set-key-test
  (is (= "OK" (redis-component/set-key test-redis-component "1" "2"))))

(deftest get-key-test
  (is (= "2" (redis-component/get-key test-redis-component "1"))))
