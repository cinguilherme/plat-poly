(ns gcc.platform.redis.generators
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

;; Spec for a non-empty string
(s/def ::non-empty-string (s/and string? seq))

;; Spec for a key, which can be either a non-empty string or an integer
(s/def ::key (s/or :string ::non-empty-string))

(s/def ::map-key (s/or :string ::non-empty-string :keyword keyword?))
(s/def ::valid-prim (s/or :string string? :int int?))

;; Helper function to limit the depth of recursive structures
(defn simple-gen []
  (s/gen (s/or :string string? :int int?)))

(defn consistent-map-gen []
  (let [simple-pair-gen (gen/tuple (s/gen ::map-key) (s/gen ::valid-prim))]
    (gen/fmap (fn [pairs] (into {} pairs))
              (gen/vector simple-pair-gen 2))))  ; Limit the number of pairs in the map

(defn simple-vec-gen []
  (gen/vector (s/gen ::valid-prim) 5))  ; Generate vectors with a limited number of simple elements


(s/def ::map (s/with-gen
               (s/map-of ::valid-prim (s/or :prim ::valid-prim :map ::map :vec ::vec))
               #(consistent-map-gen)))

(s/def ::vec (s/with-gen
               (s/coll-of (s/or :prim ::valid-prim) :kind vector?)
               #(simple-vec-gen)))

(s/def ::value (s/or :string ::non-empty-string :int int? :map ::map :vec ::vec))

(defn gen-data [size]
  (gen/sample (s/gen (s/tuple ::key ::value)) size))


(defn convert-map-keys-to-keywords [data]
  (cond
    (map? data) (into {} (map (fn [[k v]] [(keyword (str k)) v]) data))
    :else data))

(comment

  (gen/sample (s/gen ::key) 10)
  (gen/sample (s/gen ::map-key) 10)
  (gen/sample (s/gen ::valid-prim) 10)
  (gen/sample (s/gen ::map) 3)
  (gen/sample (s/gen ::vec) 50)
  (gen/sample (s/gen ::value) 50)
  
  (gen-data 5)

  (gen/sample (s/gen (s/tuple ::key ::value)) 30)

  )