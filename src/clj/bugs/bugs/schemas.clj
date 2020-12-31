(ns bugs.bugs.schemas)

(def bug
  [:map
   [:id int?]
   [:name string?]
   [:short-description string?]
   [:rating int?]])

(def new-bug
  [:map
   [:name string?]
   [:short-description string?]
   [:rating int?]])

(def get-bugs-response
  {:body [:vector bug]})

(def get-bug-request
  {:path [:map [:id int?]]})

(def get-bug-response
  {:body bug})

(def post-bugs-request
  {:body new-bug})

(def post-bugs-response
  {:body bug})
