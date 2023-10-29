(ns gcc.platform.user.interface
  (:require [gcc.platform.user.core :as core]))

(defn hello [name]
  (core/hello name))