(ns gcc.platform.pedestal.component
  (:require [com.stuartsierra.component :as component]
            [schema.core :as s]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

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
      ::http/port 8890})))

(defn start [server]
  (future (http/start server)))

(defrecord PedestalComponent [routes redis elasticsearch dynamodb postgres producer-sqs]
  component/Lifecycle

  (start [component]
    (let [components-map {:redis redis
                          :elasticsearch elasticsearch
                          :dynamodb dynamodb
                          :postgres postgres
                          :producer-sqs producer-sqs}
          server (create-server routes components-map)
          started (future (start server))]
      (assoc component
             :server server
             :started started
             :components-map components-map)))

  (stop [component]
    (when-let [server (:server component)]
      (http/stop server)
      (assoc component :stopped true))))

(s/defn new-pedestal-component [routes :- s/Any]
  (println "new-pedestal-component init with only routes, no components, make sure components/using is used next")
  (map->PedestalComponent {:routes routes}))

;; (comment

;;   (defn respond-hello [request components-map]
;;   ;; Your implementation here, using components-map if needed
;;     {:status 200 :body "Hello, world!"})

;;   (defn respond-hello-json [request components-map]
;;   ;; Your implementation here, using components-map if needed
;;     (println "respond-hello-json" components-map)
;;     {:status 200
;;      :headers {"Content-Type" "application/json"}
;;      :body (json/encode {:message "Hello, world! This is a JSON response. 🔥"})})

;;   (def simple-routes
;;     [["/greet" :get respond-hello :route-name :greet]
;;      ["/greet-json" :get respond-hello-json :route-name :greet-json]])

;;   (defn new-system []
;;     (component/system-map
;;      :pedestal (new-pedestal-component simple-routes)))

;;   (println 1)

;;   (def system (component/start (new-system)))

;;   (println system)

;;   (component/stop system))