(ns gcc.platform.pedestal-server.core
  (:require [gcc.platform.pedestal.interface :as c.pedestal])
  (:gen-class))

(defn -main [& args]
  (c.pedestal/start))