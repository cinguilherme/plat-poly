(ns gcc.platform.pedestal.interface
  (:require [gcc.platform.pedestal.component :as component]))

(defn new-pedestal-component [routes]
  (component/new-pedestal-component routes))

(defn start [component]
  (start component))

(defn stop [component]
  (stop component))
