(ns gcc.platform.envs.core
 (:require [clojure.pprint :refer [pprint]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [dotenv :refer [env]]))

(defn read-systen-envs-map []
  (let [envs (env)]
    (when envs
      (into {} (map (fn [[k v]] [(keyword k) v]) envs)))))

;; fn take vec of symbols and return map of symbols and values
(defn read-config-map [symbols]
  (let [envs (read-systen-envs-map)]
    (when envs
      (into {} (map (fn [k] [k (envs k)]) symbols)))))

(defn- read-config-file [path]
  (with-open [rdr (io/reader path)]
    (edn/read (java.io.PushbackReader. rdr))))

(defn- config-file-path []
  (let [resource-path (str "config-" (env "ENV") ".edn")
        resource (io/resource resource-path)]
    (when resource
      (subs (.toString resource) 5)))) ; Remove "file:" prefix

(defn load-config-data []
  (let [path (config-file-path)]
    (when path
      (read-config-file path))))



(comment

  (pprint (load-config-data))

  (pprint (read-config-map [:dbtype :dbname :user :password]))
  (pprint (read-systen-envs-map))

  ;;end
  )
