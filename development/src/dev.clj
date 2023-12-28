(ns dev
  (:require [gcc.platform.files.interface :as files]
            [com.stuartsierra.component :as component]
            [gcc.platform.elastic_search.interface :as es]
            [gcc.platform.elastic_search.component :as esc]))

(comment

  (def es-component (es/new-elasticsearch-component "http://localhost:9200/"))

  (esc/search-documents es-component "my-index" {:title "Test"})
  ()

  (println 1)
  
  )