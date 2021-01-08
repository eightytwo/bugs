(ns bugs.bugs.views
  (:require [bugs.layout :as layout]))

(defn bugs-list
  [req bugs tags]
  (layout/render
   "bugs.html"
   {:bugs bugs
    :tags tags
    :form-params (:form-params req)
    :errors (:form-errors req)}))
