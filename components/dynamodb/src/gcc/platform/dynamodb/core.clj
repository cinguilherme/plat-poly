(ns gcc.platform.dynamodb.core
  (:require [taoensso.faraday :as far]))


(comment

  (def client-opts
    {:access-key "test"
     :secret-key "test"

   ;; port 4566 for LocalStack
     :endpoint "http://localhost:4566"})

  (defn get-by-id [id]
    (far/get-item client-opts
                  :sample-table
                  {:id id}))

  (far/create-table
   client-opts
   :sample-table-2
   [:id :n]  ; Primary key named "id", (:n => number type)
   {:throughput {:read 1 :write 1} ; Read & write capacity (units/sec)
    :block? true ; Block thread during table creation
    })

  (far/create-table
   client-opts
   :sample-table-3
   [:id :n]  ; Primary key named "id", number type (:n)
   {:throughput {:read 1 :write 1}
    :block? true
    :gsindexes
    [{:index-name "FirstNameIndex"               ; Name of the global secondary index
      :key-schema [:first-name :s]               ; Key schema for the index (attribute and type)
      :projection {:projection-type :all}        ; Projection type (keys-only, include, all)
      :provisioned-throughput {:read 1 :write 1} ; Throughput settings for the index
      }]})

  (far/update-table
   client-opts :sample-table-3
   {:gsindexes {:operation    :create
                :name         "FirstNameIndex"
                :hash-keydef  [:first-name :s]
                :throughput   {:read 1 :write 1}}})

  (far/put-item
   client-opts
   :sample-table-3
   {:id 5 ; Remember that this is our primary (indexed) key
    :name "Fred Mercury"
    :first-name "Fred" ;; This is the attribute we're indexing
    :age 27
    :data (far/freeze {:vector    [1 2 3]
                       :set      #{1 2 3}
                       :rational (/ 22 7)
                ;; ... Any Clojure data goodness
                       })})

  (defn query-items
    ([client-opts table-name key-conds]
     (query-items client-opts table-name key-conds nil))

    ([client-opts table-name key-conds index-name]
     (let [opts (if index-name
                  {:index index-name}
                  {})]
       (far/query client-opts
                  table-name
                  key-conds
                  opts))))

  (query-items client-opts :sample-table-2 {:id [:eq 1]})
  (query-items client-opts :sample-table-3 {:first-name [:eq "Fred"]} "FirstNameIndex")

  (far/get-item client-opts
                :sample-table-2
                {:id 1})

  (far/get-item client-opts
                :sample-table-3
                {:id 5})

  (far/query client-opts
             :sample-table-2
             {:id 1})



  (far/list-tables client-opts)


  (far/describe-table client-opts :sample-table-3)
  )

