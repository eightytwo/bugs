(ns bugs.bugs.schemas)

(def bug
  {:id int? :name string? :short-description string? :rating int?})

(def new-bug
  (dissoc bug :id))

(def get-bugs-response
  {:body [bug]})

(def get-bug-request
  {:path {:id int?}})

(def get-bug-response
  {:body bug})

(def post-bugs-request
  {:body new-bug})

(def post-bugs-response
  {:body bug})
