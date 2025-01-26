(ns dev
  (:require [taoensso.carmine :as car :refer [wcar]]
            [taoensso.carmine.message-queue :as car-mq]
            [com.stuartsierra.component :as component]
            [gcc.platform.common-messaging.redis.component :as redis-component]

            ;; rabitMQ
            [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]
            ;; end rabbit

            [gcc.platform.common-messaging.in-mem-event-bus.component :as in-mem]
            [gcc.platform.common-messaging.core-async.component :as ca]

            ;; top level interface
            [gcc.platform.common-messaging.interface :as intf]
            [gcc.platform.common-messaging.protocols :as proto]))


;; rabit mq stuff
(comment

  (def ^{:const true}
    default-exchange-name "")

  (defn message-handler
    [ch {:keys [content-type delivery-tag type] :as meta} ^bytes payload]
    (println (format "[consumer] Received a message: %s, delivery tag: %d, content type: %s, type: %s"
                     (String. payload "UTF-8") delivery-tag content-type type)))


  (let [conn  (rmq/connect {:host "127.0.0.1" :port 5672})
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

;; redis stuff
(defonce my-conn-pool (car/connection-pool {})) ; Create a new stateful pool

(def     my-conn-spec {:uri "redis://localhost:6379/"})
(def     my-wcar-opts {:pool my-conn-pool, :spec my-conn-spec})

(def     my-conn-opts {:pool my-conn-pool :spec my-conn-spec})

(defmacro wcar* [& body] `(car/wcar my-wcar-opts ~@body))


  ;;  hacking with Redis gets and sets, using carmine
(comment

  ;; hacking with Redis gets and sets, using carmine

  (wcar my-wcar-opts (car/ping))

  (defonce my-conn-pool (car/connection-pool {})) ; Create a new stateful pool

  (def     my-conn-spec {:uri "redis://localhost:6379/"})
  (def     my-wcar-opts {:pool my-conn-pool, :spec my-conn-spec})

  (wcar my-wcar-opts (car/ping))

  (defmacro wcar* [& body] `(car/wcar my-wcar-opts ~@body))

  (wcar*
   (car/ping)
   (car/set "foo" "bar")
   (car/get "foo")) ;


  (wcar*
   (car/set "clj-key"
            {:bigint (bigint 31415926535897932384626433832795)
             :vec    (vec (range 5))
             :set    #{true false :a :b :c :d}
             :bytes  (byte-array 5)
       ;; ...
             })

   (car/get "clj-key"))


  (clojure.repl/doc car/sort)
  ;;
  )


;; Carmine messaging queue only hacking producer and consumer
(comment


  ;; consumer worker
  (def my-worker
    (car-mq/worker my-conn-opts "my-queue"
                   {:handler
                    (fn [{:keys [message attempt]}]
                      (try
                        (println "Received" message)
                        {:status :success}
                        (catch Throwable _
                          (println "Handler error!")
                          {:status :retry})))}))

  ;; prdoducer
  (wcar* (car-mq/enqueue "my-queue" {:message {:payload "my message!"} :metadata {:a 1 :b 2}}))

  ;; producer without wcar* macro
  (car/wcar my-wcar-opts (car-mq/enqueue "my-queue" {:message {:payload "my message!"} :metadata {:a 1 :b 2}}))



  ;; bare bones consumer
  (def my-worker
    (car-mq/worker my-conn-opts "my-queue"
                   {:handler
                    (fn [{:keys [message attempt]}]
                      (try
                        (println "Received" message)
                        {:status :success}
                        (catch Throwable _
                          (println "Handler error!")
                          {:status :retry})))})))

;; component producer hacking
(comment


  (defn handler-1 [{:keys [message attempt]}]
    (try
      (println "Received 1" message)
      {:status :success}
      (catch Throwable _
        (println "Handler error!")
        {:status :retry})))

  (defn handler-2 [{:keys [message attempt]}]
    (try
      (println "Received 2" message)
      {:status :success}
      (catch Throwable _
        (println "Handler error!")
        {:status :retry})))

  (def consumer-c (redis-component/create-redis-consumer {:uri "redis://localhost:6379/"} {}))
  (def active-consumer (component/start consumer-c))
  (component/stop active-listeners)

  consumer-c
  active-consumer

  (def active-listeners
    (intf/listen active-consumer
                 {:my-queue-xpto {:queue "my-queue-xpto"
                                  :handler handler-1
                                  :error-callback (fn [e] (println "Error callback" e))}

                  :my-queue-xpto-2 {:queue "my-queue-xpto-2"
                                    :handler handler-2
                                    :error-callback (fn [e] (println "Error callback" e))}}))


  (def producer (redis-component/create-redis-producer {:uri "redis://localhost:6379/"} {}))

  (component/start producer)

  (intf/send-message producer {:destination {:queue "my-queue-xpto-2"}
                               :message {:payload "my message!" :meta {:a 1 :b 2}}} {})



  (doseq [_ (range 5)]
    (intf/send-messages producer [{:destination {:queue "my-queue-xpto"}
                                   :message {:payload "my message!" :meta {:a 1 :b 2}}}
                                  {:destination {:queue "my-queue-xpto-2"}
                                   :message {:payload "my message!" :meta {:a 1 :b 2}}}] {:async? true}))

  ;;
  )


(comment

  (def cc (in-mem/create-in-mem-consumer (:event-bus sc) (atom {}) (atom false)))
  (def active-listeners (component/start cc))
  (intf/listen active-listeners
               {:queue "a-queue"
                :handler (fn [message]
                           (println "Received" message))})
  (intf/listen active-listeners
               {:queue "a-queue-2"
                :handler (fn [message]
                           (println "Received" message))})
  active-listeners

  (def c (in-mem/create-in-mem-producer))
  (def sc (component/start c))
  @(:event-bus sc)

  (intf/send-message sc {:destination {:queue "a-queue"}
                         :message {:payload "my message!" :meta {:a 1 :b 2}}}
                     {})

  (intf/send-message sc {:destination {:queue "a-queue-2"}
                         :message {:payload "my message for queue 2!" :meta {:a 1 :b 2}}}
                     {})

  (doseq [_ (range 10)]
    (intf/send-messages sc [{:destination {:queue "a-queue"}
                             :message {:payload "my message!" :meta {:a 1 :b 2}}}
                            {:destination {:queue "a-queue"}
                             :message {:payload "my message 34!" :meta {:a 1 :b 2}}}
                            {:destination {:queue "a-queue"}
                             :message {:payload "my message 66!" :meta {:a 1 :b 2}}}
                            {:destination {:queue "a-queue-2"}
                             :message {:payload "my message for queue 2!" :meta {:a 1 :b 2}}}]
                        {}))

  (def a (atom {}))
  (-> "a-queue" keyword)
  (swap! a assoc :queue {})
  (swap! a assoc :queue-2 {})

  (deref a)

  ;;
  )



;; core async stuff
(comment

  (def producer (ca/create-core-async-producer))
  (def started-producer (component/start producer))

  ;; The producer now has :channels as an atom:
  (:channels started-producer)
  ;; => #atom {...}


  (def consumer (ca/create-core-async-consumer (:channels started-producer)))
  (def started-consumer (component/start consumer))

  started-consumer

  (intf/listen started-consumer
               {:queue "a-queue"
                :handler (fn [message]
                           (println "Received" message))})
  (intf/listen started-consumer
               {:queue "a-queue-2"
                :handler (fn [message]
                           (println "Received" message))})
  started-consumer

  (intf/send-message started-producer {:destination {:queue "a-queue"}
                                       :message {:payload "my message!" :meta {:a 1 :b 2}}}
                     {})

  (intf/send-message started-producer {:destination {:queue "a-queue-2"}
                                       :message {:payload "my message for queue 2!" :meta {:a 1 :b 2}}}
                     {})

  (doseq [_ (range 10)]
    (intf/send-messages started-producer [{:destination {:queue "a-queue"}
                                           :message {:payload "my message!" :meta {:a 1 :b 2}}}
                                          {:destination {:queue "a-queue"}
                                           :message {:payload "my message 34!" :meta {:a 1 :b 2}}}
                                          {:destination {:queue "a-queue"}
                                           :message {:payload "my message 66!" :meta {:a 1 :b 2}}}
                                          {:destination {:queue "a-queue-2"}
                                           :message {:payload "my message for queue 2!" :meta {:a 1 :b 2}}}]
                        {}))
  ;;
  )


;; using the interface top level instructions for in memory funcionalities
(comment

  (def events-map
    {:queue-1 "a-queue"
     :queue-2 "a-queue-2"})
  (def bus (atom {}))

  (def configs
    {:bus bus
     :events-map events-map
     :consumer-map {:first {:queue "a-queue"
                            :handler (fn [message]
                                       (println "Received" message))}
                    :second {:queue "a-queue-2"
                             :handler (fn [message]
                                        (println "Received" message))}}})



  (def prod-c (intf/new-producer-component
               {:kind :in-mem
                :configs configs}))
  prod-c
  (def active-prod (component/start prod-c))
  active-prod
  (component/stop active-prod)


  ;; consumer
  (def cons-c (intf/new-consumer-component {:kind :in-mem
                                            :configs configs}))
  cons-c

  (def active-cons (component/start cons-c))
  active-cons
  (component/stop active-cons)

  #_(proto/listen active-cons
                  {:queue "a-queue"
                   :handler (fn [message]
                              (println "Received" message))})
  #_(proto/listen active-cons
                  {:queue "a-queue-2"
                   :handler (fn [message]
                              (println "Received" message))})

  (proto/send-messages active-prod [{:destination {:queue "a-queue"}
                                     :message {:payload "my message!" :meta {:a 1 :b 2}}}
                                    {:destination {:queue "a-queue-2"}
                                     :message {:payload "my message for queue 2!" :meta {:a 1 :b 2}}}]
                       {}))



;; using the interface top level instructions for REDIS
(comment

  (def configs
    {:server-info {:uri "redis://localhost:6379/"}
     :pool-settings {}
     :events-map {:queue-1 "a-queue"
                  :queue-2 "a-queue-2"}
     :consumer-map {:first {:queue "a-queue"
                            :handler (fn [message]
                                       (println "Received" message))}
                    :second {:queue "a-queue-2"
                             :handler (fn [message]
                                        (println "Received" message))}}})



  (def prod-c (intf/new-producer-component
               {:kind :redis
                :configs configs}))
  prod-c
  (def active-prod (component/start prod-c))
  active-prod
  (component/stop active-prod)


  ;; consumer
  (def cons-c (intf/new-consumer-component {:kind :redis
                                            :configs configs}))
  cons-c

  (def active-cons (component/start cons-c))
  active-cons
  (component/stop active-cons)

  #_(proto/listen active-cons
                  {:queue "a-queue"
                   :handler (fn [message]
                              (println "Received" message))})
  #_(proto/listen active-cons
                  {:queue "a-queue-2"
                   :handler (fn [message]
                              (println "Received" message))})

  (proto/send-messages active-prod [{:destination {:queue "a-queue"}
                                     :message {:payload "my message!" :meta {:a 1 :b 2}}}
                                    {:destination {:queue "a-queue-2"}
                                     :message {:payload "my message for queue 2!" :meta {:a 1 :b 2}}}]
                       {}))