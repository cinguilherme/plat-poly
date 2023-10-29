(ns gcc.platform.cli.core 
  (:require [gcc.platform.user.interface :as user])
  (:gen-class))

(defn -main [& args]
  (println (user/hello (first args)))
  (System/exit 0))