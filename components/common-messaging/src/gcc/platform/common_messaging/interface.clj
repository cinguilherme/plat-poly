(ns gcc.platform.common-messaging.interface)

(defprotocol CommonProducer
  "A protocol for sending messages to a message queue, 
   has to be generic to the point of not knowing the underlying implementation 
   and working fine with Redis, Kafka, SQS, RabbitMQ, etc.
   message is a map and at least it has a key :message and :destination each being a map 
   witch underling component implementation may require diferent keys"
  (send-message [this message]))

(defprotocol CommonConsumer
  "Start a consumer worker for a given settings
   settings is a map with the following keys: {:event-consumer-x {:handler s/fn :reciever-details s/map}}
    details will be provided by the implementation detail for each messaging technology"
  (listen [this setting]))