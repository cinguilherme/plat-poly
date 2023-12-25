(ns gcc.platform.s3.core
  (:require 
   [amazonica.aws.s3 :as s3]))


(def aws-credentials
  {:access-key "dummy-access-key"
   :secret-key "dummy-secret-key"
   :endpoint   "http://localhost:4566" ; LocalStack S3 endpoint
   :client-config {:path-style-access true} ; Enforce path-style access
   :region     "us-east-1"})
 ; or whatever your LocalStack is configured to use

(def miniio-credentials 
  {:access-key "minio"
   :secret-key "minio123"
   :endpoint   "http://localhost:9000" ; miniio S3 endpoint
   :path-style-access true})

(comment

  (s3/create-bucket
   aws-credentials
   "my-bucket")

  (s3/create-bucket
   miniio-credentials
   "my-bucket")


  (s3/list-buckets aws-credentials)
  (s3/list-buckets miniio-credentials)

  )