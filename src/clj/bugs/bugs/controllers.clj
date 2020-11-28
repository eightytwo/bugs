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
  (let [_ (:db req)
        {{:keys [body]} :parameters} req]
    ;(sql/insert! db :foo {:id 1})
    (print "Would create bug: " body)
    (let [new-bug (assoc body :id 1)]
      {:status 200
       :body   new-bug})))
