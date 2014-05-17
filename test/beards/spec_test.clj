(ns beards.spec-test
  (:require [me.raynes.fs :as fs]
            [clj-yaml.core :as yaml]
            [beards.core :as beards]
            [clojure.test :refer :all]))

(defn debug [msg arg]
  (println msg)
  (println arg)
  arg)

(deftest ^:spec mustache-template-specs
  (def spec-files (fs/glob (fs/file "." "spec-tests" "specs") "*.yml"))

  (doall
   (->> spec-files
        (map slurp)
        (map #(try (yaml/parse-string %) (catch Exception e (println (str "Failed parsing " %)))))
        (remove nil?)
        (map (fn [spec]
               (println (:overview spec))
               (doseq [test (:tests spec)]
                 (testing (:desc test)
                   (is (= (beards/render (:template test) (:data test {})) (:expected test))))))))))
