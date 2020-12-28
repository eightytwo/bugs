(ns bugs.middleware
  (:require [bugs.layout :as layout]
            [clojure.string :as str]
            [next.jdbc :as jdbc]
            [prone.middleware :as prone]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.swagger :as swagger]
            [ring.middleware.anti-forgery :as anti-forgery]
            [ring.middleware.reload :as reload]
            [ring.util.request :as request]
            [ring.util.response :as response]
            [selmer.middleware :as selmer]))

(def db
  "Middleware that opens a database transaction and adds it
  to the request map."
  {:name    ::db
   :compile (fn [{:keys [db]} _]
              (fn [handler]
                (fn [req]
                  (jdbc/with-transaction [tx db]
                    (handler (assoc req :db tx))))))})

(def api-subdomain-to-path
  "Middlware that redirects requests example.com/api/ to api.example.com."
  (fn [handler]
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
          (handler req))))))

(defn wrap-csrf [handler]
  (anti-forgery/wrap-anti-forgery
   handler
   {:error-response
    (layout/error-page
     {:status 403
      :title "Invalid anti-forgery token"})}))

(def wrap-prone
  (fn [handler]
    (prone/wrap-exceptions handler {:app-namespaces ['bugs]})))

;; Middleware that wraps all routes
(def route-middleware
  "The middleware listed here will be applied to all routes."
  [;; swagger feature
   swagger/swagger-feature
   ;; query-params & form-params
   parameters/parameters-middleware
   ;; content-negotiation
   muuntaja/format-negotiate-middleware
   ;; encoding response body
   muuntaja/format-response-middleware
   ;; exception handling
   ; exception/exception-middleware
   ;; decoding request body
   muuntaja/format-request-middleware
   ;; coercing response bodys
   coercion/coerce-response-middleware
   ;; coercing request parameters
   coercion/coerce-request-middleware
   ;; multipart
   multipart/multipart-middleware
   ;; add the database to the request, specifically a db transaction
   db])

;; Middleware that wraps the ring handler
(def base-handler-middleware
  "The middleware listed here will be applied to the ring handler
  in all environments."
  ; [[api-subdomain-to-path]]
  [])

(def dev-handler-middleware
  "The middleware listed here for the ring handler will only be applied
  in the dev environment."
  [[wrap-prone]
   [selmer/wrap-error-page]
   [reload/wrap-reload]])

(defn handler-middleware
  "The middleware to be applied to the handler, based on the environment."
  [profile]
  (-> base-handler-middleware
      (cond-> (= profile :dev)
        (concat dev-handler-middleware))))
