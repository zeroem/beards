(defproject matross/beards "0.1.0-SNAPSHOT"
  :description "An implementation of the Mustache Template Engine"
  :url "http://github.com/zeroem/beards"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]]

  :profiles {:dev {:dependencies [[clj-yaml "0.4.0"]
                                  [me.raynes/fs "1.4.4"]]}}

  :test-selectors {:default (complement :spec)
                   :spec :spec
                   :all (constantly true)})
