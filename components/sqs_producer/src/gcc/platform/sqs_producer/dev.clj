(ns gcc.platform.sqs_producer.dev
  (:require [amazonica.core :as amazonica]
            [amazonica.aws.sqs :as sqs]) 
  (:import (com.amazonaws.auth DefaultAWSCredentialsProviderChain)))