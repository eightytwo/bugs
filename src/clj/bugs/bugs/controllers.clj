(ns bugs.bugs.controllers
  (:require [bugs.db :as queries]
            [bugs.bugs.schemas :as s]
            [bugs.bugs.views :as v]))

(defn get-bugs
  [req]
  (let [db (:db req)]
    (queries/get-bugs db)))

(defn show-bugs
  [req]
  (let [bugs (get-bugs req)]
    (v/bugs-list req bugs s/tags)))

(defn get-bug
  [req]
  (let [db (:db req)
        id (get-in req [:parameters :path :id])]
    (queries/get-bug-by-id db {:id id})))

(defn create-bug
  [req]
  (let [db (:db req)
        body (val (first (select-keys (:parameters req) [:body :form])))]
    (first (queries/insert-bug db body))))
