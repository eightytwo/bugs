(ns bugs.bugs.schemas
  (:require [malli.util :as mu]))

(def tags ["safe" "caution" "dangerous"])

(def new-bug
  [:map
   [:name [:string {:min 3, :max 8}]]
   [:short-description string?]
   [:tag (into [:enum] tags)]
   [:age pos-int?]
   [:rating [:int {:min 1, :max 10}]]])

(def bug
  (mu/merge
   new-bug
   [:map
    [:id int?]
    [:created-at inst?]]))

(def get-bugs-response
  {:body [:vector bug]})

(def get-bug-request
  {:path [:map [:id int?]]})

(def get-bug-response
  {:body bug})

(def post-bugs-response
  {:body bug})
