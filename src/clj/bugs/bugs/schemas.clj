(ns bugs.bugs.schemas)

(def bug
  {:id int? :type string? :name string?})

(def new-bug
  (dissoc bug :id))

(def get-bugs-request
  {:query {:type string?}})

(def get-bugs-response
  {:body [bug]})

(def post-bugs-request
  {:body new-bug})

(def post-bugs-response
  {:body bug})
