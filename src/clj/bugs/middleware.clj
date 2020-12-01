(ns bugs.middleware
  (:require [next.jdbc :as jdbc]
            [clojure.string :as str]))

(def api-subdomain-to-path
  {:name    ::api-subdomain-to-path
   :compile (fn [_ _]
              (fn [handler _]
                (fn [req]
                  (if (str/starts-with? (:server-name req) "api.")
                    (handler (assoc req :uri (str "/api" (:uri req))))
                    (handler req)))))})

(def db
  {:name    ::db
   :compile (fn [{:keys [db]} _]
              (fn [handler]
                (fn [req]
                  (jdbc/with-transaction [tx db]
                    (handler (assoc req :db tx))))))})
