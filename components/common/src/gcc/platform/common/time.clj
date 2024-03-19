(ns gcc.platform.common.time
  (:require [clj-time.core :as time]))

(defn now []
  (System/currentTimeMillis))

;; timestamp miliseconds to ISO 8601 UTC format
(defn timestamp-to-iso-8601 [timestamp]
  (let [date (java.util.Date. timestamp)]
    (-> date
        (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        (.setTimeZone (java.util.TimeZone/getTimeZone "UTC"))
        (.format date))))

(comment

  (time/millis)

  (time/date-time 2022 1 1 0 0 0)

  (str (time/now))
  

  (-> (now)
      str
      timestamp-to-iso-8601)

  )