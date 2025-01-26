(ns gcc.platform.common-messaging.core)

(def kind-set #{:redis :rabbitmq :in-mem :in-mem-async :core-async})

(defn exists? [set item] (contains? set item))

(defn check-kind [kind]
  (when (not (exists? kind-set kind))
    (throw (IllegalArgumentException. (str "Invalid kind: " kind)))))
