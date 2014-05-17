(ns beards.core-test
  (:require [clojure.test :refer :all]
            [beards.core :refer :all]))



(deftest basic-functionality-tests
  (testing "I can parse a plain string"
    (is (= (render "herp") "herp")))

  (testing "I can parse a simple variable"
    (is (= (render "{{var}}" {:var "herp"}) "herp"))))
