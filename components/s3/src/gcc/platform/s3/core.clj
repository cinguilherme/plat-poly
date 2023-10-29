(ns gcc.platform.s3.core
  (:require 
   [amazonica.core :as amazonica]
   [amazonica.aws.s3 :as s3]))


(def aws-credentials
  {:access-key "dummy-access-key"
   :secret-key "dummy-secret-key"
   :endpoint   "http://localhost:4566" ; LocalStack S3 endpoint
   :client-config {:path-style-access true} ; Enforce path-style access
   :region     "us-east-1"})
 ; or whatever your LocalStack is configured to use


(comment

  (s3/create-bucket
   aws-credentials
   "my-bucket")

  

  (s3/list-buckets aws-credentials)

  )