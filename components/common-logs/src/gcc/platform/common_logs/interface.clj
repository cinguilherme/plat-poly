(ns gcc.platform.common-logs.interface)

(defprotocol CommonLogs
  (log
    [this message]
    [this message level]
    [this message level metadata]
    [this message level metadata exception]))