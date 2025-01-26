(ns gcc.platform.common-messaging.core-async.component
  (:require
   [clojure.core.async :as async]
   [com.stuartsierra.component :as component]
   [gcc.platform.common-messaging.protocols :as protocol]
   [gcc.platform.common-messaging.core-async.core :as core]))

(defrecord CoreAsyncProducer [channels]
  component/Lifecycle
  (start [this]
    (assoc this :channels (or channels (atom {}))))
  (stop [this]
    (doseq [[_ ch] @channels]
      (async/close! ch))
    (assoc this :channels nil))

  protocol/CommonProducer
  (send-message [this message ops]
    (let [queue   (-> message :destination :queue keyword)
          payload (-> message :message)
          ch      (core/get-or-create-chan channels queue ops)]
      (if (get ops :async? false)
        (async/put! ch payload)
        (async/>!! ch payload)))
    nil)

  (send-messages [this messages ops]
    (doseq [m messages]
      (let [queue   (-> m :destination :queue keyword)
            payload (-> m :message)
            ch      (core/get-or-create-chan channels queue ops)]
        (if (get ops :async? false)
          (async/put! ch payload)
          (async/>!! ch payload))))
    nil))

(defn create-core-async-producer []
  (map->CoreAsyncProducer {}))

(defrecord CoreAsyncConsumer [channels threads-atom stop?-atom]
  component/Lifecycle
  (start [this]
    (let [threads-atom (or threads-atom (atom {}))
          stop?-atom   (or stop?-atom (atom false))]
      (assoc this :threads-atom threads-atom
             :stop?-atom   stop?-atom)))
  (stop [this]
    (reset! stop?-atom true)
    ;; optionally close channels or let the producer handle it
    (assoc this :threads-atom nil :stop?-atom nil))

  protocol/CommonConsumer 
  (listen [this {:keys [queue handler]}]
    (let [queue-k (keyword queue)
          ch      (get @channels queue-k)]
      (when ch
        ;; Spin up a go-loop that reads from the channel until stop?-atom is true
        (let [loop-chan (async/go
                          (while (not @stop?-atom)
                            (when-let [msg (async/<! ch)]
                              (handler msg))))]
          (swap! threads-atom assoc queue-k loop-chan))))
    nil))

(defn create-core-async-consumer [channels-atom]
  ;; inject the same channels atom used by CoreAsyncProducer
  (map->CoreAsyncConsumer {:channels channels-atom}))