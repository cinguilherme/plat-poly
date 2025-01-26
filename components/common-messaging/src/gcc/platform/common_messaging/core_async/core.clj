(ns gcc.platform.common-messaging.core-async.core
  (:require [clojure.core.async :as async]))

(defn get-or-create-chan
  [channels-atom queue {:keys [buffer-size buffer-type]
                        :or   {buffer-size 100
                               buffer-type :fixed}}]
  (or (get @channels-atom queue)
      (let [buffer (case buffer-type
                     :dropping (async/dropping-buffer buffer-size)
                     :sliding  (async/sliding-buffer buffer-size)
                     ;; Default/fixed buffer
                     (async/buffer buffer-size))
            ch     (async/chan buffer)]
        (swap! channels-atom
               (fn [m]
                 (if (contains? m queue)
                   m
                   (assoc m queue ch))))
        (get @channels-atom queue))))