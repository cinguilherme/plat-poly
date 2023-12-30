(ns gcc.platform.elastic_search.interface
  (:require [com.stuartsierra.component :as component]
            [gcc.platform.elastic_search.component :as es-component]))

(defn new-elasticsearch-component [endpoint]
  (es-component/new-elasticsearch-component endpoint))

(defn index-document [component index-type document-id document]
  (es-component/index-document component index-type document-id document))

(defn search-documents [component index-type query]
  (es-component/search-documents component index-type query))
