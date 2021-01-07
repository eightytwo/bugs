(ns bugs.middleware
  (:require [bugs.config :as config]
            [bugs.layout :as layout]
            [clojure.string :as str]
            [next.jdbc :as jdbc]
            [prone.middleware :as prone]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.swagger :as swagger]
            [ring.adapter.undertow.middleware.session :as session]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.gzip :as gzip]
            [ring.middleware.reload :refer [wrap-reload]]
            [selmer.middleware :as selmer])
  (:import [clojure.lang ExceptionInfo]))

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

(defn wrap-csrf
  [handler]
  (wrap-anti-forgery
   handler
   {:error-response
    (layout/error-page
     {:status 403
      :title "Invalid anti-forgery token"})}))

(defn csp-header
  [profile]
  (let [defaults   config/csp-header-default
        script-src (:script-src defaults)
        all-items  (if (= profile :dev)
                     ;; Allow JS in dev for prone
                     (assoc defaults :script-src (conj script-src "'self'"))
                     defaults)]
    (str/join
     " "
     (map (fn [[k v]] (str (name k) " " (str/join " " v) ";"))
          all-items))))

(defn wrap-security-headers
  [profile]
  (fn [handler]
    (fn [req]
      (let [res (handler req)]
        (-> res
            (assoc-in [:headers "Referrer-Policy"] "strict-origin")
            (cond-> (not (api-request? req))
              (assoc-in
               [:headers "Content-Security-Policy"]
               (csp-header profile))))))))

(def wrap-session
  (fn [handler]
    (session/wrap-session
     handler
     {:cookie-name config/cookie-name
      :http-only true})))

(def wrap-ring-defaults
  (fn [handler]
    (wrap-defaults
     handler
     (-> site-defaults
         (assoc-in [:security :anti-forgery] false)
         (assoc-in [:security :frame-options] :deny)
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

(defn handle-production-exception
  "Handle uncaught exceptions in the production environment."
  [handler]
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
            (layout/error-page error)))))))

(defn wrap-exceptions
  "Middleware that handles any uncaught exceptions based on the environment."
  [profile]
  (if (contains? #{:dev :test} profile)
    handle-dev-exception
    handle-production-exception))

(defn handle-request-coercion-exception
  [data]
  (let [req (:request data)
        req-method (:request-method req)
        route-data (get-in req [:reitit.core/match :data req-method])
        view-fn (:view-fn route-data)]
    (view-fn req)))

(defn wrap-request-coercion-exceptions
  [handler]
  (fn [req]
    (try
      (handler req)
      (catch ExceptionInfo ex
        (if (= (:type (ex-data ex)) :reitit.coercion/request-coercion)
          (handle-request-coercion-exception (ex-data ex))
          (throw ex))))))

(def api-routes-middleware
  "The middleware listed here will be applied to all routes."
  [swagger/swagger-feature               ;; swagger feature
   parameters/parameters-middleware      ;; query-params & form-params
   muuntaja/format-negotiate-middleware  ;; content-negotiation
   muuntaja/format-response-middleware   ;; encoding response body
   coercion/coerce-exceptions-middleware ;; handle coercion exceptions
   muuntaja/format-request-middleware    ;; decoding request body
   coercion/coerce-response-middleware   ;; coercing response bodys
   coercion/coerce-request-middleware    ;; coercing request parameters
   multipart/multipart-middleware        ;; multipart
   db])                                  ;; provide a database transaction

(def web-routes-middleware
  [wrap-csrf
   parameters/parameters-middleware      ;; query-params & form-params
   muuntaja/format-negotiate-middleware  ;; content-negotiation
   muuntaja/format-response-middleware   ;; encoding response body
   db                                    ;; provide a database transaction
   wrap-request-coercion-exceptions      ;; handle request coercion exceptions
   muuntaja/format-request-middleware    ;; decoding request body
   coercion/coerce-request-middleware    ;; coercing request parameters
   coercion/coerce-response-middleware]) ;; coercing response bodys

(defn handler-middleware
  "The middleware to be applied to the ring handler.

  The middleware in the vector returned is applied to the request from
  bottom to top. This means the first item in the vector (the outermost
  layer of the wrapping) will be the first to process a request and
  be the last to touch the response."
  [profile]
  (-> [[(wrap-exceptions profile)]         ;; Handle any exceptions gracefully
       [gzip/wrap-gzip]                    ;; Compress the response
       [(wrap-security-headers profile)]   ;; Add security headers
       [wrap-ring-defaults]                ;; Apply industry standard defaults
       [wrap-session]                      ;; Enable session handling
       [wrap-flash]]                       ;; Enable flash sessions
      (cond-> (= profile :dev)
        (concat [[wrap-prone]              ;; Present exceptions nicely in the browser
                 [selmer/wrap-error-page]  ;; Present HTML template errors nicely
                 [wrap-reload]]))))        ;; Reload Clojure code when changed
