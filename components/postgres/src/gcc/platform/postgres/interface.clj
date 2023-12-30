(ns gcc.platform.postgres.interface)

(defprotocol PostgresComponentCore 
  (execute! [component sql] "Executes a SQL statement without additional arguments")
  (execute-params! [component sql params] "Executes a SQL statement with additional parameters"))