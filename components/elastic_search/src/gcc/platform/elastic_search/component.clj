(ns gcc.platform.elastic_search.component
  (:require [com.stuartsierra.component :as component]
            [gcc.platform.elastic_search.core :as es-core]))

(defrecord ElasticsearchComponent [endpoint]
  component/Lifecycle
  (start [this]
    (println "Elasticsearch component starting")
    this)
  (stop [this]
    (println "Elasticsearch component stopping")
    this))

(defn index-document [component index-type document-id document]
  (es-core/index-document (:endpoint component) index-type document-id document))

(defn search-documents [component index-type query]
  (es-core/search-documents (:endpoint component) index-type query))

(defn new-elasticsearch-component [endpoint]
  (map->ElasticsearchComponent {:endpoint endpoint}))