(ns beards.renderer-test
  (:require [clojure.test :refer :all]
            [beards.renderer :refer :all]
            [beards.parser :refer [parse]]))


(defn simple-render [s token]
  (second (render-token {:s s} token {})))

(deftest basic-token-rendering
  (testing "comments are blank"
    (is (clojure.string/blank? (simple-render "sdjfsdfds"
                                              {:type :comment}))))

  (testing "String are returned verbatim"
    (is (= (simple-render "This is a Test"
                          {:type :string :start-pos 0 :end-pos 4})
           "This"))))

(deftest render-test
  (testing "I can render a collection of tokens"
    (let [s "This is a {{foo}} string {{!comment}}"
          state (parse s)
          result (render state {:foo "bar"})]
      (is (= result "This is a bar string "))))

  (testing "A Collection section will render"
    (let [s "{{#foo}}{{bar}}{{/foo}}"
          state (parse s)
          result (render state {:foo (take 3 (repeat {:bar "baz"}))})]
      (is (= result "bazbazbaz"))))

  (testing "A false value will not render"
    (let [s "{{#foo}}{{bar}}{{/foo}}"
          state (parse s)
          result (render state {:foo nil})]
      (is (= result ""))))

  (testing "A false value will not render"
    (let [s "{{^foo}}missing{{/foo}}"
          state (parse s)
          result (render state {:foo nil})]
      (is (= result "missing"))))

  (testing "html gets escaped properly"
    (let [s "{{& foo}}{{foo}}{{{foo}}}"
          state (parse s)
          result (render state {:foo "\""})]
      (is (= result "\"&quot;\"")))))
