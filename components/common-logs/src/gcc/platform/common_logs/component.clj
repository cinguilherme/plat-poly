(ns gcc.platform.common-logs.component
  (:require [com.stuartsierra.component :as component]
            [gcc.platform.common-logs.interface :as common-logs]
            [gcc.platform.common-logs.core :as core]))

(defrecord DefaultLogs []
  component/Lifecycle
  (start [this]
    (assoc this :logger core/default-logger))

  (stop [this]
    (assoc this :logger nil))

  common-logs/CommonLogs
  (log [this message]
    ((-> this :logger :info) message)
    this)

  (log [this message level]
    (when-let [log-fn (get (:logger this) level)]
      (log-fn message))
    this)

  (log [this message level metadata]
    (when-let [log-fn (get (:logger this) level)]
      (log-fn {:message message :metadata metadata}))
    this)

  (log [this message level metadata exception]
    (when-let [log-fn (get (:logger this) level)]
      (log-fn {:message message :metadata metadata :exception exception}))
    this))

(defn create-default-logs []
  (map->DefaultLogs {}))