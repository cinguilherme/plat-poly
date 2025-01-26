(ns gcc.platform.common-messaging.in-mem-event-bus.core 
  (:require
   [clojure.pprint :refer [pprint]]))

(defn poll-queue!
  "Continuously pops messages from the given `queue` key in `event-bus`
   and invokes `handler` on each.  Stops when `stop?` atom becomes true."
  [event-bus queue-key handler stop?]
  (future
    (while (not @stop?)
      (let [message (atom nil)]
        ;; Atomically pop one message off the queue (if any):
        (swap! event-bus
               (fn [bus]
                 (let [q (get bus queue-key clojure.lang.PersistentQueue/EMPTY)]
                   (if (empty? q)
                     bus
                     (do
                       (reset! message (peek q))    ;; capture the message
                       (assoc bus queue-key (pop q)))))))
        ;; If we got a message, invoke the handler:
        (when-let [m @message]
          (handler m)))
      (Thread/sleep 50))))

(defn iterate-consumer-map-add-listeners! 
  [threads-atom stop?-atom consumer-map event-bus]
  (doseq [[_ {:keys [queue handler]}] consumer-map]
    (pprint queue)
    (pprint handler)
    (let [fut (poll-queue! event-bus (keyword queue) handler stop?-atom)]
      (swap! threads-atom assoc queue fut))))