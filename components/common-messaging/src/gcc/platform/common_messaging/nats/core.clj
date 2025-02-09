(ns gcc.platform.common-messaging.nats.core
  (:require [clojure.string :as c.string]
            [nats.stream :as stream]))


;; logics to be moved to core
(defn ensure-stream-exists [conn {:keys [name subjects retention-policy]}]
  (try
    (stream/get-stream-info conn name) ;; Check if stream already exists
    (println (str "[NATSProducer] Stream `" name "` already exists. Skipping creation."))
    (catch Exception e
      (println (str "[NATSProducer] Creating stream `" name "`"))
      (stream/create-stream conn
                            {:nats.stream/name name
                             :nats.stream/subjects subjects
                             :nats.stream/retention-policy (keyword (str "nats.retention-policy/" retention-policy))}))))

(defn subject-matches? [wildcard subject]
  (let [pattern (-> wildcard
                    (c.string/replace "." "\\.")  ;; Escape .
                    (c.string/replace ">" ".*")   ;; Convert > to regex wildcard
                    (re-pattern))]
    (re-matches pattern subject)))

(defn find-stream-for-queue [config queue]
  (some (fn [{:keys [name subjects]}]
          (when (some #(subject-matches? % queue) subjects)
            name))
        (:queues config)))

(defn sanitize-consumer-name [queue]
  (-> queue
      (clojure.string/replace "." "-")  ;; Replace dots
      (clojure.string/replace ">" "")   ;; Remove > wildcard (if exists)
      (clojure.string/replace "/" "-"))) ;; Replace slashes (if needed)

;;end