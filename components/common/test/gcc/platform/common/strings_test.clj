(ns gcc.platform.common.strings-test
  (:require [clojure.test :as test :refer :all]
            [gcc.platform.common.strings :as strings]))

(deftest upperCaseFirstCharacter-test
  (is (= (strings/upperCaseFirstCharacter "hello world") "Hello world")))

(deftest lowerCaseFirstCharacter-test
  (is (= (strings/lowerCaseFirstCharacter "Hello world") "hello world")))

(deftest joinVectorWithHyfen-test
  (is (= (strings/joinVectorWithHyfen ["hello" "world"]) "hello-world")))

(deftest kebab-separator-fn-test
  (is (= (strings/kebab-separator-fn "helloWorld") ["hello" "world"])))

  (deftest snake-case-test
    (is (= (strings/snake-case "hello world") "hello_world"))
    (is (= (strings/snake-case "hello-world") "hello_world"))
    (is (= (strings/snake-case "hello_world") "hello_world"))
    (is (= (strings/snake-case "helloWorld") "helloWorld"))
    (is (= (strings/snake-case "helloWorld!") "helloWorld_")))

  (deftest camel-case-test
    (is (= (strings/camel-case "hello world") "helloWorld"))
    (is (= (strings/camel-case "hello-world") "helloWorld"))
    (is (= (strings/camel-case "hello_world") "helloWorld"))
    (is (= (strings/camel-case "helloWorld") "helloWorld"))
    (is (= (strings/camel-case "helloWorld!") "helloWorld")))

  (deftest kebab-case-test
    (is (= (strings/kebab-case "hello world") "hello-world"))
    (is (= (strings/kebab-case "hello-world") "hello-world"))
    (is (= (strings/kebab-case "hello_world") "hello-world"))
    (is (= (strings/kebab-case "helloWorld") "hello-world"))
    (is (= (strings/kebab-case "helloWorld!") "hello-world!")))
