(ns gcc.platform.common.interface
  (:require [gcc.platform.common.core :as core]))

(defn load-config-for-env []
  (core/load-config-data))
