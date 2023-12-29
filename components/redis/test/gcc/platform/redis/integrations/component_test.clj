(ns gcc.platform.redis.integrations.component-test
 (:require [clojure.test :as test :refer :all]
           [clojure.spec.alpha :as s]
           [clojure.spec.gen.alpha :as gen]
           [gcc.platform.redis.component :as redis]
           [com.stuartsierra.component :as component]
           [gcc.platform.redis.interface :as redis-component]
           [gcc.platform.redis.generators :as generators]
           [cheshire.core :as json]))


(def test-redis-component
  (redis/new-redis-component "localhost" 6379))

(component/start test-redis-component)

(deftest set-key-test
  (is (= "OK" (redis-component/set-key test-redis-component "1" "2")))
  (is (= "OK" (redis-component/set-key test-redis-component "10" "number 2")))
  (is (= "OK" (redis-component/set-key test-redis-component "11" {:a 1 :b "2" :c ["1" "2" "3"]})))
  (is (= "OK" (redis-component/set-key test-redis-component 4 "4")))
  (is (= "OK" (redis-component/set-key test-redis-component "5" [3 2 1])))
  (is (= "OK" (redis-component/set-key test-redis-component "X" {:arr [3 2 1] :str "hello"})))
  (is (= "2" (redis-component/get-key test-redis-component "1")))
  (is (= "number 2" (redis-component/get-key test-redis-component "10")))
  (is (= {:a 1 :b "2" :c ["1" "2" "3"]} (redis-component/get-key test-redis-component "11")))
  (is (= "4" (redis-component/get-key test-redis-component 4)))
  (is (= [3 2 1] (redis-component/get-key test-redis-component "5")))
  (is (= {:arr [3 2 1] :str "hello"} (redis-component/get-key test-redis-component "X"))))


;; Generative tests

(deftest set-key-test-gen
  (testing "set-key with generated values"
    (doseq [generated-data (generators/gen-data 200)]
      (let [[gen-key gen-value] generated-data]
        (is (= "OK" (redis-component/set-key test-redis-component gen-key gen-value)))))))

(deftest get-key-test-gen
  (testing "get-key with generated values"
    (doseq [generated-data (generators/gen-data 200)]
      (let [[gen-key gen-value] generated-data
            str-val (json/generate-string gen-value)]
        (redis-component/set-key test-redis-component gen-key str-val)
        (is (= (generators/convert-map-keys-to-keywords gen-value) 
               (json/parse-string (redis-component/get-key test-redis-component gen-key) true)))))))