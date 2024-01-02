(ns gcc.platform.pedestal.component
  (:require [com.stuartsierra.component :as component]
            [schema.core :as s]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [gcc.platform.sqs_producer.interface :as sqs-producer]))

;; Define the schema for the Redis pool configuration
(def ComponentsMap
  {:dynamodb (s/maybe s/Any)
   :postgres (s/maybe s/Any)
   :producer-sqs (s/maybe s/Any) 
   :redis (s/maybe s/Any)
   :elasticsearch (s/maybe s/Any)})

(defn inject-components [handler components-map]
  (fn [request]
    (handler request components-map)))

(defn wrap-routes [simple-routes components-map]
  (let [inject (fn [handler] (inject-components handler components-map))
        wrapped-routes (map #(update % 2 inject) simple-routes)]
    (route/expand-routes (set wrapped-routes))))

(defn create-server [simple-routes components-map]
  (let [wrapped-routes (wrap-routes simple-routes components-map)]
    (http/create-server
     {::http/routes wrapped-routes
      ::http/type :jetty
      ::http/host "0.0.0.0"
      ::http/port 8890})))

(defn start [server]
  (future (http/start server)))

(defrecord PedestalComponent [routes redis elasticsearch dynamodb postgres sqs-producer]
  component/Lifecycle

  (start [component]
    (println "Starting PedestalComponent")
    (let [components-map {:redis redis
                          :elasticsearch elasticsearch
                          :dynamodb dynamodb
                          :postgres postgres
                          :sqs-producer sqs-producer}
          server (create-server routes components-map)
          started (future (start server))]
      (assoc component
             :server server
             :started started
             :components-map components-map)))

  (stop [component]
    (println "Stopping PedestalComponent")
    (when-let [server (:server component)]
      (http/stop server)
      (assoc component :stopped true))))

(s/defn new-pedestal-component [routes :- s/Any]
  (println "new-pedestal-component init with only routes, no components, make sure components/using is used next")
  (map->PedestalComponent {:routes routes}))
