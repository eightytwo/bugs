(ns bugs.bugs.controllers)
  ;(:require [next.jdbc.sql :as sql]
  ;          [next.jdbc :as jdbc])

(defn get-bugs
  [req]
  (let [_ (:db req)
        {{{:keys [type]} :query} :parameters} req]
    ;(with-open [conn (jdbc/get-connection db)]
    ;  (let [record (sql/find-by-keys conn :foo {:id 1})]
    ;    (println "!! Found record: " record)))
    (print "Querying for bugs of type" type)
    {:status 200
     :body   [{:id 1 :type "Grasshopper" :name "Joe"}
              {:id 2 :type "Dragon Fly" :name "Jane"}]}))

(defn post-bugs
  [req]
  (let [_ (:db req)
        {{:keys [body]} :parameters} req]
    ;(sql/insert! db :foo {:id 1})
    (print "Would create bug: " body)
    (let [new-bug (assoc body :id 1)]
      {:status 200
       :body   new-bug})))
