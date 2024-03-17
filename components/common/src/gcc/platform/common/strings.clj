(ns gcc.platform.common.strings
  (:require [clojure.string :as str]))

(defn upperCaseFirstCharacter [s]
       (str (clojure.string/upper-case (subs s 0 1)) (subs s 1)))

(defn lowerCaseFirstCharacter [s]
       (str (clojure.string/lower-case (subs s 0 1)) (subs s 1)))

(defn joinVectorWithHyfen [v]
  (str/join "-" v))

(defn kebab-separator-fn [s] 
  (->> (str/split s #"[\s-_]") 
         (map (fn [word] 
                  (if (re-find #"[A-Z]" word)
                 (str/split word #"(?=[A-Z])")
                 word)))
       flatten
       (map str/lower-case)
       vec
       ))
  

(comment

  (kebab-separator-fn "helloWorld")

  (upperCaseFirstCharacter "hello world")

  (joinVectorWithHyfen ["hello" "world"])
  )
  

;; fn to snake-case a string
(defn snake-case [s]
  (clojure.string/replace s #"\W" "_"))

;; fn to camelCase a string, consider underscores and hyphens as word separators [-,_,\W, ]
(defn camel-case [s]
  (let [s (clojure.string/replace s #"\W" " ")]
    (->> (str/split s #"[\s-_]") 
         (map upperCaseFirstCharacter) 
         (apply str)
         lowerCaseFirstCharacter)))

;; fn to kebab-case a string
(defn kebab-case [s]
  (-> s kebab-separator-fn 
      joinVectorWithHyfen))
  