(ns beards.core-test
  (:require [clojure.test :refer :all]
            [beards.core :refer :all]))

(defn prep-form-str [s]
  (-> (new-state s) parse-to-start-delim))

(deftest find-first-instance-of-test
  (testing "No matches return nil"
    (is (= (find-first-instance-of (new-state "herp") [])
           nil)))

  (testing "A single token can be found"
    (is (= (find-first-instance-of (new-state "{{{") ["{{{"])
           ["{{{" 0])))

  (testing "Competing tokens only matches the first"
    (is (= (find-first-instance-of (new-state "herp<%{{{") ["<%" "{{{"])
           ["<%" 4]))))

(deftest parse-to-start-delim-test
  (testing "No matching delims returns the whole string"
    (let [start-state (new-state "herp")
          end-state (parse-to-start-delim start-state)
          token (first (:tokens end-state))]
      (is (= (count (:tokens end-state)) 1))
      (is (= (:start-pos token) 0))
      (is (= (:end-pos token) 4))))

  (testing "State gets updated when delim is found"
    (let [start-state (new-state "Herp {{derp}} foo")
          end-state (parse-to-start-delim start-state)
          {:keys [tokens current-delims s pos]} end-state
          {:keys [start-pos end-pos]} (first tokens)]
      (is (= (count tokens) 1))
      (is (= (subs s start-pos pos) "Herp "))
      (is (= current-delims default-delims))
      (is (= (subs s pos) "{{derp}} foo")))))

(deftest parse-form-type-test
  (testing "special no-escape delims"
    (let [end-state (prep-form-str "{{{unescaped}}}")]
      (is (= (parse-form-type end-state) :no-escape))))

  (testing "at least one of the indicators works"
    (let [end-state (prep-form-str "{{!comment}}")]
      (is (= (parse-form-type end-state) :comment))))

  (testing "default case works"
    (let [end-state (prep-form-str "{{lookup}}")]
      (is (= (parse-form-type end-state) :lookup)))))

(deftest parse-form-expression-test
  (testing "that it works..."
    (let [s "omg a form {{# zomg }}"
          state (prep-form-str s)]
      (is (= (parse-form-expression state (count s)) "zomg")))))

(deftest parse-form-test
  (testing "A token can be extracted"
    (let [s "{{#section-expression}}"
          state (prep-form-str s)
          {:keys [tokens]} (parse-form state)
          token (first tokens)]
      (is (= (count tokens) 1))
      (is (= (:type token) :start-section))
      (is (= (:expression token) "section-expression"))
      (is (= (:original token) s)))))


#_(deftest basic-functionality-tests
  (testing "I can parse a plain string"
    (is (= (render "herp") "herp")))

  (testing "I can parse a simple variable"
    (is (= (render "{{var}}" {:var "herp"}) "herp"))))
