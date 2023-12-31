(ns gcc.platform.envs.core
 (:require [clojure.pprint :refer [pprint]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [dotenv :refer [env]]))

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
