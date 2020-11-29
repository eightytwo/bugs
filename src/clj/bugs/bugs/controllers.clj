(ns bugs.bugs.controllers
  (:require [bugs.db :as queries]))

(defn get-bugs
  [req]
  (let [db (:db req)]
    {:status 200
     :body   (queries/get-bugs db)}))

(defn get-bug
  [req]
  (let [db (:db req)
        id (:id (:path (:parameters req)))]
    {:status 200
     :body   (queries/get-bug-by-id db {:id id})}))

(defn post-bugs
  [req]
  (let [db (:db req)
        body (:body (:parameters req))
        bug (first (queries/insert-bug db body))]
    {:status 200
     :body   (dissoc bug :created-at)}))
