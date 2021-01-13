(ns bugs.bugs.views
  (:require [bugs.layout :as layout]))

(defn bugs-list
  [req bugs tags]
  (let [errors (:form-errors req)]
    (layout/render
     "bugs.html"
     {:bugs        bugs
      :tags        tags
      :form-params (if errors (:form-params req) {})
      :errors      errors})))
