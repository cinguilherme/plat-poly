(ns gcc.platform.common-messaging.in-mem-event-bus.component
  (:require
   [clojure.pprint :refer [pprint]]
   [com.stuartsierra.component :as component]
   [gcc.platform.common-messaging.interface :as intf]
   [gcc.platform.common-messaging.in-mem-event-bus.core :as core]))

(defn tap [x]
  (pprint x)
  x)

;; Producer
(defrecord InMemEventBusProducer [event-bus]
  component/Lifecycle
  (start [this]
    (assoc this :event-bus (atom {})))
  (stop [this]
    (reset! (:event-bus this) {})
    (dissoc this :event-bus))

  intf/CommonProducer
  (send-message [this message ops]
    (let [destination (-> message :destination :queue keyword)
          payload     (-> message :message)]
      (swap! (:event-bus this)
             (fn [bus]
               (let [q (get bus destination clojure.lang.PersistentQueue/EMPTY)]
                 (assoc bus destination (conj q payload))))))
    nil)

  (send-messages [this messages ops]
    ;; Batch version, enqueues everything in one swap.
    (swap! (:event-bus this)
           (fn [bus]
             (reduce
              (fn [acc message]
                (let [destination (-> message :destination :queue keyword)
                      payload     (-> message :message)
                      q           (get acc destination clojure.lang.PersistentQueue/EMPTY)]
                  (assoc acc destination (conj q payload))))
              bus
              messages)))
    nil))

(defn create-in-mem-producer []
  (->InMemEventBusProducer {}))

;; Consumer
(defrecord InMemEventBusConsumer [event-bus threads-atom stop?-atom]
  component/Lifecycle
  (start [this]
    ;; On start, initialize controlling atoms if they aren’t set yet:
    (let [stop?-atom   (or stop?-atom (atom false))
          threads-atom (or threads-atom (atom {}))]
      (assoc this
             :stop?-atom stop?-atom
             :threads-atom threads-atom)))

  (stop [this]
    (reset! stop?-atom true)
    (doseq [[_ fut] @threads-atom]
      (future-cancel fut))
    (reset! threads-atom {})
    (assoc this :stop?-atom nil
           :threads-atom nil))

  intf/CommonConsumer
  (listen [this settings]
    ;; Listen for messages on the queue specified by :queue, 
    ;; calling the function in :handler each time a message arrives.
    (let [queue   (-> settings :queue keyword)
          handler (-> settings :handler)
          fut     (core/poll-queue! event-bus queue handler stop?-atom)]
      (swap! threads-atom assoc queue fut))
    nil))

(defn create-in-mem-consumer [bus threads-atom stop?-atom]
  (->InMemEventBusConsumer bus threads-atom stop?-atom))

