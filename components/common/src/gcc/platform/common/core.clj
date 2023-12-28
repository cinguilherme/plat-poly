(ns gcc.platform.common.core
  (:require [clojure.pprint :refer [pprint]]))

(defmacro tap
  "Pretty prints the input and returns it."
  [input]
  `(do (pprint ~input)
       ~input))

(comment
  
  (defn example-function []
  (let [data {:a 1 :b 2 :c 3}]
    (tap data)))  ; This will pretty-print `data` and return it
  
  (example-function)

  )