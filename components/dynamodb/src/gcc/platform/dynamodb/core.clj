(ns gcc.platform.dynamodb.core
  (:require [taoensso.faraday :as far]))

(def client-opts
  {:access-key "dummy-access-key"
   :secret-key "dummy-secret-key"

   ;; port 4566 for LocalStack
   :endpoint "http://localhost:4566" 
   })


(comment

  (far/create-table
   client-opts
   :sample-table
   [:id :n]  ; Primary key named "id", (:n => number type)
   {:throughput {:read 1 :write 1} ; Read & write capacity (units/sec)
    :block? true ; Block thread during table creation
    })

  (far/put-item
   client-opts
   :sample-table
   {:id 1 ; Remember that this is our primary (indexed) key
    :name "Steve"
    :age 22
    :data (far/freeze {:vector    [1 2 3]
                       :set      #{1 2 3}
                       :rational (/ 22 7)
                ;; ... Any Clojure data goodness
                       })})

  (far/get-item client-opts
                :sample-table
                {:id 1})



  (far/list-tables client-opts)
  
  )

