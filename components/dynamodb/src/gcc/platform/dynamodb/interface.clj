(ns gcc.platform.dynamodb.interface
  (:require [gcc.platform.dynamodb.core :as core]))

(defn sample-by-id! [id]
  (core/get-by-id id))

(comment

  (sample-by-id! 1)

  )
