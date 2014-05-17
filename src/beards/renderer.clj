(ns beards.renderer)

(defmulti render-token (fn [s cursor token data] (:type token)))

(defmethod render-token :comment [s cursor token data] [cursor ""])

(defmethod render-token :string [s
                                 cursor
                                 {:keys [start-pos end-pos] :as token}
                                 _]
  [cursor (subs s start-pos end-pos)])

(defmethod render-token :default [s cursor token data] [cursor "DEFAULT"])


(defn render [s tokens data]
  (loop [tokens tokens
         cursor []
         result ""]
    (if-let [token (first tokens)]
      (let [[cursor rs] (render-token s cursor token data)]
        (recur (rest tokens) cursor (str result rs)))
      result)))
