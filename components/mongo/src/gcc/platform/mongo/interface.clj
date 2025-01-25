(ns gcc.platform.mongo.interface)

(defprotocol DocDB
  "Document DB's like MongoDB, DynamoDB, etc. 
    in general should provide these operations or no op them"
  (doc-connect [this])
  (doc-disconnect [this conn])
  (doc-create-collection [this collection ops])
  (doc-insert [this collection map-document])
  (doc-update [this collection query update])
  (doc-delete [this collection query])
  (doc-find-maps [this collection query])
  (doc-find-map [this collection query]))