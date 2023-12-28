(ns gcc.platform.elastic_search.interface
  (:require [com.stuartsierra.component :as component]
            [gcc.platform.elastic_search.component :as es-component]
            [gcc.platform.elastic_search.core :as core]))

(defn new-elasticsearch-component [endpoint]
  (es-component/new-elasticsearch-component endpoint))
