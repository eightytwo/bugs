(ns bugs.middleware
  (:require [clojure.string :as str]
            [next.jdbc :as jdbc]
            [ring.util.request :as request]
            [ring.util.response :as response]))

(def api-subdomain-to-path
  {:name    ::api-subdomain-to-path
   :compile (fn [_ _]
              (fn [handler _]
                (fn [req]
                  ;; If the call is to api.example.com then append the /api directory.
                  ;; This is needed because all API routes fall under /api.
                  (if (str/starts-with? (:server-name req) "api.")
                    (handler (assoc req :uri (str "/api" (:uri req))))

                    ;; If the call is to example.com/api then redirect to api.example.com,
                    ;; otherwise let it pass through.
                    (if (str/starts-with? (:uri req) "/api/")
                      (let [host (get-in req [:headers "host"])
                            new-url (str/replace-first (request/request-url req)
                                                       (str host "/api")
                                                       (str "api." host))
                            code (if (= (:request-method req) :get)
                                   :moved-permanently
                                   :permanent-redirect)]
                        (response/redirect new-url code))
                      (handler req))))))})

(def db
  {:name    ::db
   :compile (fn [{:keys [db]} _]
              (fn [handler]
                (fn [req]
                  (jdbc/with-transaction [tx db]
                    (handler (assoc req :db tx))))))})
