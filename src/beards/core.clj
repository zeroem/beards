(ns beards.core
  (:require [beards.parser :as parser]
            [beards.renderer :as renderer]))

(defn render
  ([template] (render template {}))
  ([template data]
     (-> template
         parser/parse
         (renderer/render data))))
