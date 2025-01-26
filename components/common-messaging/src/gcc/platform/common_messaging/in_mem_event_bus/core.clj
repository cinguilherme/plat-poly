(ns gcc.platform.common-messaging.in-mem-event-bus.core)

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