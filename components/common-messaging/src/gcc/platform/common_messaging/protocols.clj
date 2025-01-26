(ns gcc.platform.common-messaging.protocols)

(defprotocol CommonProducer
  "A protocol for sending messages to a message queue, 
   has to be generic to the point of not knowing the underlying implementation 
   and working fine with Redis, Kafka, SQS, RabbitMQ, etc.
   message is a map and at least it has a key :message and :destination each being a map 
   witch underling component implementation may require diferent keys
    ops is a config map that should be adhered by all implementations with the following keys
    :async? s/Bool default false, this will indicate if the producer will be async or not for blocking or non blocking operation"
  (send-message
    [this message]
    [this message ops])
  (send-messages
    [this messages]
    [this messages ops]))

(defprotocol CommonConsumer
  "Start a consumer worker for a given settings
   settings is a map with the following keys: {:event-consumer-x {:handler s/fn :reciever-details s/map}}
    details will be provided by the implementation detail for each messaging technology
    and optionally an `ops` map for implementation-specific options."
  (listen
    [this setting]
    [this setting ops]))