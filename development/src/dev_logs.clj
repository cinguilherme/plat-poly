(ns dev-logs
  (:require [gcc.platform.common-logs.interface :as common-logs]
            [gcc.platform.common-logs.component :as clogs]
            [com.stuartsierra.component :as component]))

(comment

  (def l (clogs/create-default-logs))
  (def sl (component/start l))
  
  l
  sl

  sl

  (let []
    (common-logs/log sl "Syslem slarted")
    (common-logs/log sl "Warning message" :warn)
    (common-logs/log sl "Debugging info" :debug {:user "admin"})
    (common-logs/log sl "Error occurred" :error {:cause "Unexpected"}))

  ;;
  )