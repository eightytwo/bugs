(ns bugs.bugs.schemas
  (:require [malli.util :as mu]))

(def tags ["safe" "caution" "dangerous"])

(def bug
  [:map
   [:id int?]
   [:name [:string {:min 3, :max 8}]]
   [:short-description string?]
   [:tag [:enum "safe" "caution" "dangerous"]]
   [:age pos-int?]
   [:rating [:int {:min 1, :max 10}]]])

(def new-bug
  (mu/dissoc bug :id))

(def get-bugs-response
  {:body [:vector bug]})

(def get-bug-request
  {:path [:map [:id int?]]})

(def get-bug-response
  {:body bug})

(def post-bugs-response
  {:body bug})
