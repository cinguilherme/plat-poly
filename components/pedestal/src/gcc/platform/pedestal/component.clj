(ns gcc.platform.pedestal.component 
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defn create-server [routes]
  (http/create-server
   {::http/routes routes
    ::http/type   :jetty
    ::http/port   8890}))

(defn start [server]
  (future (http/start server)))

(defrecord PedestalComponent [routes components-map]
  component/Lifecycle

  (start [component]
    (let [server (create-server routes)
          started (future (start server))]
      (assoc component
             :server server
             :started started
             :components-map components-map)))

  (stop [component]
    (when-let [server (:server component)]
      (http/stop server)
      (assoc component :stopped true))))

(defn new-pedestal-component [routes components-map]
  (map->PedestalComponent {:routes routes :components-map components-map}))

(comment

  (defn respond-hello [request]
    {:status 200 :body "Hello, world!"})

  (def routes
    (route/expand-routes
     #{["/greet" :get respond-hello :route-name :greet]}))

  (defn new-system []
    (component/system-map
     :pedestal (new-pedestal-component routes {})))

  (println 1)

  (def system (component/start (new-system)))

  (println system)

  (component/stop system)


  )