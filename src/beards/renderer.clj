(ns beards.renderer)

(declare render)
(defn make-cursor [token]
  (->> (clojure.string/split (:expression token) #"\.")
       (map keyword)))

(defmulti render-token (fn [state token data] (:type token)))

(defmethod render-token :comment [state token data] [state ""])
(defmethod render-token :change-delims [state & _] [state ""])

(defmethod render-token :string [state
                                 {:keys [start-pos end-pos] :as token}
                                 _]
  [state (subs (:s state) start-pos end-pos)])

(defmethod render-token :lookup [state token data]
  (let [cursor (make-cursor token)]
    [state (str (get-in data cursor))]))

(defn capture-section [state token]
  (let [cursor (make-cursor token)]
    (loop [exit-state state
           section-state (assoc state :tokens [])]
      (if-let [t (first (:tokens exit-state))]
        (if (= (:type t) :end-section)
          (if (= (:expression t) (:expression token))
            [(update-in exit-state [:tokens] rest) section-state]
            (throw (IllegalArgumentException.
                    (str "Expected end of `"
                         (:expression token)
                         "` but found "
                         (:expression t)))))
          (recur (update-in exit-state [:tokens] rest)
                 (update-in section-state [:tokens] conj t)))
        (throw (IllegalArgumentException. "EOF before end of section"))))))

(defmethod render-token :start-section [state token data]
  (let [target (get-in data (make-cursor token))
        [exit-state section-state] (capture-section state token)]
    (cond
     (map? target)
     [exit-state (render section-state target)]

     ((some-fn vector? list? seq?) target)
     [exit-state
      (->> target
           (map (fn [d] (render section-state d)))
           (apply str))

      ((some-fn empty? false?) target)
      [exit-state ""]])))

(defmethod render-token :stop-section [& _]
  (throw (RuntimeException. "We should never get here.")))

(defmethod render-token :default [state token data] [state "DEFAULT"])


(defn render [state data]
  (loop [state state
         result ""]
    (if-let [token (first (:tokens state))]
      (let [state (update-in state [:tokens] rest)
            [state rs] (render-token state token data)]
        (recur state (str result rs)))
      result)))
