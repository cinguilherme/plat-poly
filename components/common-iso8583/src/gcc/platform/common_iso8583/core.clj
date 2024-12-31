(ns gcc.platform.common-iso8583.core)

(defn extract-mti [message]
  (subs message 0 4))

(defn extract-bitmap [message]
  (subs message 4 20))

(defn hex-to-binary [hex]
  (let [binary (format "%64s" (.toString (new java.math.BigInteger hex, 16)) 2)]
    (.replace binary " " "0")))

(defn extract-data-elements [message binary-bitmap]
  (let [elements {}
        pan-present? (= (nth binary-bitmap 1) \1)
        processing-code-present? (= (nth binary-bitmap 2) \1)
        pan (when pan-present? (subs message 20 36)) ; Example positions for illustration
        processing-code (when processing-code-present? (subs message 36 42))]
    (cond-> elements
      pan-present? (assoc :pan pan)
      processing-code-present? (assoc :processing-code processing-code))))

(defn active-data-elements [binary-bitmap]
  (keep-indexed (fn [index bit]
                  (when (= bit \1)
                    (inc index))) binary-bitmap))
