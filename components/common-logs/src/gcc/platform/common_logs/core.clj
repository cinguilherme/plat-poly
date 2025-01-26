(ns gcc.platform.common-logs.core
  (:require [taoensso.timbre :as timbre]))

(def default-logger {:info (fn [& args] (timbre/info args))
                     :warn (fn [& args] (timbre/warn args))
                     :debug (fn [& args] (timbre/debug args))
                     :error (fn [& args] (timbre/error args))})

(comment

  ((-> default-logger :info) "System started")
  ((-> default-logger :warn) "Warning message")
  ((-> default-logger :debug) "Debugging info")
  ((-> default-logger :error) "Error occurred")
  ((-> default-logger :error) {:message "Error occurred" :cause "Unexpected"})


  ;;
  )
