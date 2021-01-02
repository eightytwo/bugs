(ns bugs.bugs.views
  (:require [bugs.layout :as layout]))

(defn bugs-list
  [bugs tags]
  (layout/render
   "bugs.html"
   {:bugs bugs :tags tags}))
