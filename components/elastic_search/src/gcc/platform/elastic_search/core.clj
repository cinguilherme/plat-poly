(ns gcc.platform.elastic_search.core
  (:require [clj-http.client :as client]
            [cheshire.core :as json]))

(defn index-document [endpoint index-type document-id document]
  (client/put (str endpoint index-type "/_doc/" document-id)
              {:body (json/generate-string document)
               :headers {"Content-Type" "application/json"}}))

(defn search-documents [endpoint index-type query]
  (-> (client/get (str endpoint index-type "/_search")
                  {:body (json/generate-string {:query {:match query}})
                   :headers {"Content-Type" "application/json"}})
      :body
      (json/parse-string true)))

(comment
  
  (def devEndpoint "http://localhost:9200/")

  ;; Index a new document
  (index-document devEndpoint "my-index" "1" {:title "Test Document" :content "This is a test."})
  (index-document devEndpoint "my-index" "2" {:title "Test Document 2" :content "This is a test 2."})
  (index-document devEndpoint "my-index" "3" {:title "Test Document 3" :content "This is a test 3."})

  ;; Search for documents
  (search-documents devEndpoint "my-index" {:title "Test"})
)