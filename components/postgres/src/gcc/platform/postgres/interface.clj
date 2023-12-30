(ns gcc.platform.postgres.interface
  (:require [gcc.platform.postgres.component :as component]))

(defn new-postgres-component [config]
  (component/new-postgres-component config))

(defn execute! [component sql]
  "Executes a SQL statement without additional arguments"
  (component/execute! component sql))

(defn execute-params! [component sql params] 
  "Executes a SQL statement with additional parameters"
  (component/execute-params! component sql params))