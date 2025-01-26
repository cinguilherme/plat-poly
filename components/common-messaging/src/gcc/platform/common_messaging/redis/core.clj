(ns gcc.platform.common-messaging.redis.core
  (:require [clojure.pprint :as pprint]
            [taoensso.carmine :as car :refer [wcar]]
            [taoensso.carmine.message-queue :as car-mq]))

(defn tap [v]
  (pprint/pprint  v)
  v)

(defonce my-conn-pool (car/connection-pool {})) ; Create a new stateful pool

(def     my-conn-spec {:uri "redis://localhost:6379/"})
(defn    conn-spec [uri] {:uri uri})

(def     my-wcar-opts {:pool my-conn-pool, :spec my-conn-spec})
(def     my-conn-opts {:pool my-conn-pool :spec my-conn-spec})

(defmacro wcar* [& body] `(car/wcar my-wcar-opts ~@body))

    ;;  hacking with Redis gets and sets, using carmine
(comment

  (defonce my-conn-pool (car/connection-pool {})) ; Create a new stateful pool

  (def     my-conn-spec {:uri "redis://localhost:6379/"})
  (def     my-wcar-opts {:pool my-conn-pool, :spec my-conn-spec})

  (def     my-conn-opts {:pool my-conn-pool :spec my-conn-spec})

  (defmacro wcar* [& body] `(car/wcar my-wcar-opts ~@body))

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

  (defonce my-conn-pool (car/connection-pool {})) ; Create a new stateful pool

  (def     my-conn-spec {:uri "redis://localhost:6379/"})
  (def     my-wcar-opts {:pool my-conn-pool, :spec my-conn-spec})

  (def     my-conn-opts {:pool my-conn-pool :spec my-conn-spec})

  (defmacro wcar* [& body] `(car/wcar my-wcar-opts ~@body))

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
  (wcar* (car-mq/enqueue "my-queue" {:message {:payload "my message!"} :metadata {:a 1 :b 2}})))

