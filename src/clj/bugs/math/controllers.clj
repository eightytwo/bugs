(ns bugs.math.controllers
  (:require [next.jdbc.sql :as sql]
            [next.jdbc :as jdbc]))

(defn get-add
  [req]
  (let [db (:db req)
        {{{:keys [x y]} :query} :parameters} req]
    (with-open [conn (jdbc/get-connection db)]
      (let [record (sql/find-by-keys conn :foo {:id 1})]
        (println "!! Found record: " record)))
    {:status 200
     :body   {:total (+ x y)}}))

(defn post-add
  [req]
  (let [db (:db req)
        {{{:keys [x y]} :body} :parameters} req]
    (sql/insert! db :foo {:id 1})
    {:status 200
     :body   {:total (+ x y)}}))
