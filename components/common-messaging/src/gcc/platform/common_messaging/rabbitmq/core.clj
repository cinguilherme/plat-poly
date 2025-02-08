(ns gcc.platform.common-messaging.rabbitmq.core
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]))

(defn init-connection [host port]
  (let [conn  (rmq/connect {:host host :port port})
        ch    (lch/open conn)]
    {:conn conn :ch ch}))

(defn init-consumer [ch {:keys [queue handler]}]
  (lq/declare ch queue {:exclusive false :auto-delete true})
  (lc/subscribe ch queue handler {:auto-ack true}))

(defn init-consumers [ch consumers]
  (mapv #(init-consumer ch %) consumers))

(defn publish-message [ch queue message]
  (lb/publish ch "" queue message {:content-type "text/plain"}))
