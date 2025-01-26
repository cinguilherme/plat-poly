(ns gcc.platform.common-messaging.in-mem-event-bus.component
  (:require
   [clojure.pprint :refer [pprint]]
   [com.stuartsierra.component :as component]
   [gcc.platform.common-messaging.protocols :as proto]
   [gcc.platform.common-messaging.in-mem-event-bus.core :as core]))

(defn tap [x]
  (pprint x)
  x)

;; Producer
(defrecord InMemEventBusProducer [events-map bus]
  
  component/Lifecycle
  
  (start [this]
    (println "In Mem Event Bus Producer starting, this has no real state since it only requires the event-bus to exist")
    (assoc this :event-bus bus :events-map events-map))
  (stop [this]
    (println "In Mem Event Bus Producer stopping, removing the event-bus")
    (reset! (:event-bus this) {})
    (dissoc this :event-bus))

  proto/CommonProducer
  
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

(defn create-in-mem-producer 
  ([]
   (->InMemEventBusProducer {} (atom {})))
  ([events-map]
   (->InMemEventBusProducer events-map (atom {})))
  ([events-map bus]
   (->InMemEventBusProducer events-map bus)))

;; Consumer
(defrecord InMemEventBusConsumer [event-bus threads-atom stop?-atom auto-start? events-map consumer-map]
  component/Lifecycle
  (start [this]
    (println "In Mem Event Bus Consumer starting")
    ;; On start, initialize controlling atoms if they aren’t set yet:
    (let [stop?-atom   (or stop?-atom (atom false))
          threads-atom (or threads-atom (atom {}))]
      (assoc this
             :stop?-atom stop?-atom
             :threads-atom threads-atom)))

  (stop [this]
    (println "In Mem Event Bus Consumer stopping")
    (reset! stop?-atom true)
    (doseq [[_ fut] @threads-atom]
      (future-cancel fut))
    (reset! threads-atom {})
    (assoc this :stop?-atom nil
           :threads-atom nil))

  proto/CommonConsumer
  (listen [this settings]
    ;; Listen for messages on the queue specified by :queue, 
    ;; calling the function in :handler each time a message arrives.
    (let [queue   (-> settings :queue keyword)
          handler (-> settings :handler)
          fut     (core/poll-queue! event-bus queue handler stop?-atom)]
      (swap! threads-atom assoc queue fut))
    nil))

(defn create-in-mem-consumer 
  ([bus threads-atom stop?-atom]
   (->InMemEventBusConsumer bus threads-atom stop?-atom false {} {}))
  ([bus threads-atom stop?-atom events-map consumer-map]
   (->InMemEventBusConsumer bus threads-atom stop?-atom false events-map consumer-map)))

