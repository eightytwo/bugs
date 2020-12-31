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
            [ring.adapter.undertow.middleware.session :as session]
            [ring.middleware.anti-forgery :as anti-forgery]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.reload :as reload]
            [ring.util.request :as request]
            [ring.util.response :as response]
            [selmer.middleware :as selmer]))

(defn api-request?
  "Helper function to determine if a request is for the API or from a browser."
  [req]
  (str/starts-with? (:server-name req) "api."))

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
      (if (api-request? req)
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

(def wrap-session
  (fn [handler]
    (session/wrap-session handler {:http-only true})))

(def wrap-ring-defaults
  (fn [handler]
    (wrap-defaults
     handler
     (-> site-defaults
         (assoc-in [:security :anti-forgery] false)
         (dissoc :session)))))

(def wrap-prone
  "Use prone for displaying errors nicely in the browser."
  (fn [handler]
    (prone/wrap-exceptions
     handler
     {:app-namespaces ['bugs]
      :skip-prone?    (fn [req] (api-request? req))})))

(defn handle-dev-exception
  "Handle uncaught exceptions in the development environment.

  prone handles exceptions that occur when the application is accessed via
  the browser. This function handles exceptions that occur when accessing
  the application via the API."
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

(def route-middleware
  "The middleware listed here will be applied to all routes."
  [swagger/swagger-feature               ;; swagger feature
   parameters/parameters-middleware      ;; query-params & form-params
   muuntaja/format-negotiate-middleware  ;; content-negotiation
   muuntaja/format-response-middleware   ;; encoding response body
   muuntaja/format-request-middleware    ;; decoding request body
   coercion/coerce-response-middleware   ;; coercing response bodys
   coercion/coerce-request-middleware    ;; coercing request parameters
   multipart/multipart-middleware        ;; multipart
   db])                                  ;; provide a database transaction

(defn handler-middleware
  "The middleware to be applied to the ring handler.

  The middleware in the vector returned is applied to the request from
  bottom to top. This means the first item in the vector (the outermost
  layer of the wrapping) will be the first to process a request and
  be the last to touch the response."
  [profile]
  (-> [[(wrap-exceptions profile)]         ;; Handle any exceptions gracefully
       [api-subdomain-to-path]             ;; Redirect example.com/api to api.example.com
       [wrap-ring-defaults]                ;; Apply industry standard defaults
       [wrap-session]]                     ;; Enable session handling
      (cond-> (= profile :dev)
        (concat [[wrap-prone]              ;; Present exceptions nicely in the browser
                 [selmer/wrap-error-page]  ;; Present HTML template errors nicely
                 [reload/wrap-reload]])))) ;; Reload Clojure code when changed
