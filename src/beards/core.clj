(ns beards.core
  (:require [beards.parser :as parser]))

(defn render* [tokens data]
  (apply str tokens))

(defn render
  ([template] (render template {}))
  ([template data]
     (let [tokenized (parser/parse template)]
       (render* tokenized data))))
