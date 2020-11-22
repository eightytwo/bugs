(ns bugs.math.schemas)

(def get-add-request
  {:query {:x int?, :y int?}})

(def post-add-request
  {:body {:x int?, :y int?}})

(def add-response
  {:body {:total int?}})
