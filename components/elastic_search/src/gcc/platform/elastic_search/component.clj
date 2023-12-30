(ns gcc.platform.elastic_search.component
  (:require [com.stuartsierra.component :as component]
            [gcc.platform.elastic_search.core :as es-core]))

(defprotocol ElasticsearchComponentCore
  (index-document [component index-type document-id document] "Indexes a document in Elasticsearch")
  (search-documents [component index-type query] "Searches for documents in Elasticsearch"))

(defrecord ElasticsearchComponent [endpoint]
  component/Lifecycle
  ElasticsearchComponentCore

  (start [this]
    (println "Elasticsearch component starting")
    this)
  (stop [this]
    (println "Elasticsearch component stopping")
    this)

  ;; ElasticsearchComponentCore
  (index-document [this index-type document-id document]
    (es-core/index-document (:endpoint this) index-type document-id document))

  (search-documents [this index-type query]
    (es-core/search-documents (:endpoint this) index-type query)))

(defn new-elasticsearch-component [endpoint]
  (map->ElasticsearchComponent {:endpoint endpoint}))