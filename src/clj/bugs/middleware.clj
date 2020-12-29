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
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.reload :as reload]
            [ring.util.request :as request]
            [ring.util.response :as response]
            [selmer.middleware :as selmer]))

(defn api-request?
  "Helper function to determine if a request is for the API or from a browser."
  [req]
  (str/starts-with? (:uri req) "/api/"))

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
        (if (api-request? req)
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

(def wrap-ring-defaults
  (fn [handler]
    (wrap-defaults
     handler
     (-> site-defaults
         (assoc-in [:security :anti-forgery] false)))))

(def wrap-prone
  "Use prone for displaying errors nicely in the browser."
  (fn [handler]
    (prone/wrap-exceptions
     handler
     {:app-namespaces ['bugs]
      :skip-prone?    (fn [req] (api-request? req))})))

(defn handle-dev-exception
  "Handle uncaught exceptions in the development environment."
  [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (layout/error-json {:status 500 :error (Throwable->map e)})))))

(def handle-production-exception
  "Handle uncaught exceptions in the production environment."
  (fn [handler]
    (fn [req]
      (try
        (handler req)
        (catch Throwable _
          (let [error
                {:status  500
                 :title   "Well, that's embarrassing!"
                 :message "It looks like an unsolved bug reared its head."}]
            (if (api-request? req)
              (layout/error-json error)
              (layout/error-page error))))))))

(defn wrap-exceptions
  "Middleware that handles any uncaught exceptions based on the environment."
  [profile]
  (if (= profile :dev)
    handle-dev-exception
    handle-production-exception))

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
   ;; decoding request body
   muuntaja/format-request-middleware
   ;; coercing response bodys
   coercion/coerce-response-middleware
   ;; coercing request parameters
   coercion/coerce-request-middleware
   ;; multipart
   multipart/multipart-middleware
   ;; Add the database to the request, specifically a db transaction
   db])

;; Middleware that wraps the ring handler
(defn base-handler-middleware
  "The middleware listed here will be applied to the ring handler
  in all environments."
  [profile]
  ;; ----------
  ;; The middleware in the vector below is applied to the request from
  ;; bottom to top. This means the first item in the vector (the outermost
  ;; layer of the wrapping) will be executed first.
  ;; ----------
  [;; Redirect example.com/api to api.example.com
   [api-subdomain-to-path]
   ;; Apply industry standard defaults for web applications
   [wrap-ring-defaults]
   ;; Handle any exceptions gracefully
   [(wrap-exceptions profile)]])

(def dev-handler-middleware
  "The middleware listed here for the ring handler will only be applied
  in the dev environment."
  ;; ----------
  ;; The middleware in the vector below is applied to the request from
  ;; bottom to top. This means the first item in the vector (the outermost
  ;; layer of the wrapping) will be executed first.
  ;; ----------
  [;; Present errors and stacktraces nicely in the browser
   [wrap-prone]
   ;; Present HTML template errors nicely in the browser
   [selmer/wrap-error-page]
   ;; Reload Clojure code when changed
   [reload/wrap-reload]])

(defn handler-middleware
  "The middleware to be applied to the handler, based on the environment."
  [profile]
  (-> (base-handler-middleware profile)
      (cond-> (= profile :dev)
        (concat dev-handler-middleware))))
