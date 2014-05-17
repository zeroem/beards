(ns beards.renderer-test
  (:require [clojure.test :refer :all]
            [beards.renderer :refer :all]))


(defn simple-render [s token]
  (second (render-token s [] token {})))

(deftest basic-token-rendering
  (testing "comments are blank"
    (is (clojure.string/blank? (simple-render "sdjfsdfds"
                                              {:type :comment}))))

  (testing "String are returned verbatim"
    (is (= (simple-render "This is a Test"
                          {:type :string :start-pos 0 :end-pos 4})
           "This"))))
