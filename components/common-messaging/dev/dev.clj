(ns dev
  (:require [taoensso.carmine :as car :refer [wcar]]
            [taoensso.carmine.message-queue :as car-mq]
            [com.stuartsierra.component :as component]
            [gcc.platform.common-messaging.redis.component :as redis-component]
            [gcc.platform.common-messaging.interface :as intf]))

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


  )

;; component producer hacking
(comment

  (def consumer-c (redis-component/create-redis-consumer {:uri "redis://localhost:6379/"} {}))
  (component/start consumer-c)
  (intf/listen consumer-c {:queue "my-queue-xpto"
                           :error-callback (fn [e] (println "Error callback" e))
                           :handler
                           (fn [{:keys [message attempt]}]
                             (try
                               (println "Received" message "!! from consumer component")
                               {:status :success}
                               (catch Throwable _
                                 (println "Handler error! from consumer component")
                                 {:status :retry})))})

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

  (def producer (redis-component/create-redis-producer {:uri "redis://localhost:6379/"} {}))

  (component/start producer)

  (intf/send-message producer {:destination {:queue "my-queue-xpto"} 
                               :message {:payload "my message!" :meta {:a 1 :b 2}} })
  )