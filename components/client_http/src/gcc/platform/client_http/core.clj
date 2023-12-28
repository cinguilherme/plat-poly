(ns gcc.platform.client_http.core
  (:require [clj-http.client :as client]
            [cheshire.core :as json]))

(comment
  (def url "https://dummy.restapiexample.com/api/v1/employees")
  (def urlPost "https://dummy.restapiexample.com/api/v1/create")

;; Example GET request
  (defn fetch-data [endpoint]
    (let [response (client/get endpoint)
          body-str (:body response)
          body-map (json/parse-string body-str true)]  ; 'true' for keywordizing keys
      body-map))

;;   (defn fetch-data []
;;     (client/get "https://dummy.restapiexample.com/api/v1/employees"))

;; Example POST request with JSON payload
  (defn post-data [payload endpoint]
    (-> (client/post endpoint
                     {:body (json/generate-string payload)
                      :headers {"Content-Type" "application/json"}})
        :body
        (json/parse-string true)))

  (fetch-data url)

  (post-data {:name "John Doe" :salary 100000 :age 30} urlPost)
  )