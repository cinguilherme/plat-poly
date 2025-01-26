(ns gcc.platform.common-messaging.rabbitmq.core
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]))



;; rabit mq stuff
(comment
  
  (def ^{:const true}
    default-exchange-name "")
  
  (defn message-handler
    [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
    (println (format "[consumer] Received a message: %s, delivery tag: %d, content type: %s, type: %s"
                     (String. payload "UTF-8") delivery-tag content-type type)))


  (defn init [host port]
    (let [conn  (rmq/connect {:host host :port port})
          ch    (lch/open conn)]
      {:conn conn :ch ch}))
  
  (defn init-consumer [ch {:keys [queue handler]}] 
    (lc/subscribe ch queue handler {:auto-ack true}))
  
  (defn init-consumers [ch consumers]
    (mapv #(init-consumer ch %) consumers))


  
  (let [
        conn  (rmq/connect {:host "127.0.0.1" :port 5672})
        ch    (lch/open conn)
        qname "langohr.examples.hello-world"]
    (println (format "[main] Connected. Channel id: %d" (.getChannelNumber ch)))
    (lq/declare ch qname {:exclusive false :auto-delete true})
    (lc/subscribe ch qname message-handler {:auto-ack true})
    (lb/publish ch default-exchange-name qname "Hello!" {:content-type "text/plain" :type "greetings.hi"})
    (lb/publish ch default-exchange-name qname "Hello Second!" {:content-type "text/plain" :type "greetings.hi"})
    (lb/publish ch default-exchange-name qname "Hello Third!" {:content-type "text/plain" :type "greetings.hi"})
    (Thread/sleep 3000)
    (println "[main] Disconnecting...")
    (rmq/close ch)
    (rmq/close conn))


  ;; end
  )