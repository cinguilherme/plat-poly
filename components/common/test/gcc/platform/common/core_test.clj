(ns gcc.platform.common.core-test
  (:require [clojure.test :as test :refer :all]
            [gcc.platform.common.core :as core]))

(deftest dummy-test
  (is (= 1 1)))

(deftest tap-test
  (is (= (core/tap 1) 1))
  (is (= (core/tap "a") "a"))
  (is (= (core/tap {:a 1}) {:a 1}))
  (is (= (core/tap [1 2 3]) [1 2 3])))