(ns beards.parser)

(def no-escape-delims ["{{{" "}}}"])
(def default-delims ["{{" "}}"])

(def section-types {\&              :no-escape
                    \>              :partial
                    \#              :start-section
                    \/              :end-section
                    \^              :start-inverse-section
                    \!              :comment
                    \=              :change-delims})

(defn new-state
  [s] {:s s :delims default-delims :tokens [] :pos 0})

(defn merge-state [& states] (apply merge states))

(defn start-delim
  ([delims] (first delims))
  ([delims & args]
     (map first (conj args delims))))

(defn end-delim
  ([delims] (second delims))
  ([delims & args]
     (map second (conj args delims))))

(defn change-delims [[l-delim r-delim]]
  [(str l-delim "=") (str "=" r-delim)])

(defn valid-delims? [delims]
  (and (every? #(= % -1) (map #(. % indexOf "=") delims))
       (not-any? identity (map #(re-find #"\s" %) delims))))

(defn find-first-instance-of
  ([state tokens] (find-first-instance-of state tokens 0))
  ([{:keys [s pos] :as state} tokens offset]
      (let [delim-pos second]
        (->> tokens
             (map (fn [token] [token (. s indexOf token (+ pos offset))]))
             (remove #(= (end-delim %1) -1))
             (reduce (fn min-pos
                       ([])
                       ([l r] (if (< (delim-pos l) (delim-pos r)) l r))))))))

(defn string-token [state stop]
  {:type :string
   :start-pos (:pos state)
   :end-pos stop})

(defn parse-to-start-delim [{:keys [s delims tokens pos] :as state}]
  (let [[found-delim next-pos]
        (find-first-instance-of state (start-delim delims no-escape-delims))]
    (let [end-pos (or next-pos (count s))
          delims (if found-delim
                   (if (= found-delim (start-delim delims))
                     delims
                     no-escape-delims)
                   delims)]
      (merge-state state {:pos next-pos
                          :tokens (if (= next-pos pos)
                                    tokens
                                    (conj tokens
                                          (string-token state end-pos)))
                          :current-delims delims}))))


(defn char-after-delim [{:keys [s pos current-delims]}]
  (get s (+ pos (count (first current-delims)))))

(defn parse-form-type [{:keys [current-delims] :as state}]
  (if (= current-delims no-escape-delims)
    :no-escape
    (get section-types
         (char-after-delim state)
         :lookup)))

(defn trim-first-char? [{:keys [current-delims] :as state}]
  (if (= current-delims no-escape-delims)
    false
    (get section-types
         (char-after-delim state))))

(defn parse-form-expression [{:keys [s pos current-delims] :as state} stop]
  (clojure.string/trim
   (subs s
         (+ pos
            (count (start-delim current-delims))
            (if (trim-first-char? state) 1 0))
         (- stop (count (end-delim current-delims))))))

(defn form-token [{:keys [s pos current-delims] :as state} stop]
  (let [base-token {:type (parse-form-type state)
                    :original (subs s pos stop)
                    :start-pos pos}
        stop-expression (if (= (:type base-token) :change-delims)
                          (dec stop)
                          stop)]
    (merge base-token {:expression (parse-form-expression state stop-expression)
                       :stop-pos stop})))

(defn parse-new-delims [token]
  ;; Gonna need some syntax checking here
  (clojure.string/split (:expression token) #" "))

(defn parse-form [{:keys [s delims tokens current-delims] :as state}]
  (let [end-delim (end-delim current-delims)
        [found-delim end-pos]
        (find-first-instance-of state
                                (concat no-escape-delims current-delims)
                                (count end-delim))]
    (if (not (= found-delim end-delim))
      (throw (IllegalArgumentException.
              (str "Syntax error. Expected: "
                    end-delim
                    ", but found: "
                    found-delim))))
    (let [after-delim-pos (+ end-pos (count end-delim))
          token (form-token state after-delim-pos)
          new-state {:pos after-delim-pos
                     :tokens (conj tokens token)}
          final-state (if (= (:type token) :change-delims)
                        (assoc new-state :delims (parse-new-delims token))
                        new-state)]
      (merge-state state final-state))))

(defn parse [template]
  (loop [state (new-state template)]
    (let [next-state (parse-to-start-delim state)]
      (if (not (:pos next-state))
        next-state
        (recur (parse-form next-state))))))
