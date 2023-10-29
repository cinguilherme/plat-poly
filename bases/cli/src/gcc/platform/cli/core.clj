(ns gcc.platform.cli.core 
  (:require [gcc.platform.user.interface :as user]
            [gcc.platform.dynamodb.interface :as dynamodb])
  (:gen-class))

(defn -main [& args]
  (println (user/hello (first args)))
  (println (dynamodb/sample-by-id! 1))
  (System/exit 0))