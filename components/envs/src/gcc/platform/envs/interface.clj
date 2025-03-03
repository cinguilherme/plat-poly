(ns gcc.platform.envs.interface
  (:require [gcc.platform.envs.core :as core]))

(defn load-config-for-env []
  (core/load-config-data))

(defn load-config-for-env-plus-envs [extra-keys]
  (core/load-config-for-env-plus-envs extra-keys))