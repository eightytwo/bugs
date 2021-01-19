(ns bugs.middleware
  (:require [bugs.config :as config]
            [bugs.layout :as layout]
            [clojure.string :as str]
            [jdbc-ring-session.core :refer [jdbc-store]]
            [next.jdbc :as jdbc]
            [prone.middleware :as prone]
            [reitit.coercion :as reitit-coercion]
            [reitit.ring.coercion :as ring-coercion]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.swagger :as swagger]
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
                  (let [method (:request-method req)
                        write (contains? #{:post :put :delete} method)]
                    (jdbc/with-transaction [tx db {:read-only (not write)}]
                      (handler (assoc req :db tx)))))))})

(defn wrap-csrf
  [handler]
  (wrap-anti-forgery
   handler
   {:error-response
    (layout/error-page
     {:status 403
      :title "Invalid anti-forgery token"})}))

(defn csp-header
  "Format the Content Security Policy header value"
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
  "Middleware that adds security headers to the response"
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

(defn wrap-ring-defaults
  [db]
  (fn [handler]
    (wrap-defaults
     handler
     (-> site-defaults
         (assoc-in [:security :anti-forgery] false)
         (assoc-in [:security :frame-options] :deny)
         (assoc-in [:session :cookie-name] config/cookie-name)
         (assoc-in [:session :store] (jdbc-store db))))))

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
  "Extract the human error messages of a request coercion exception and
  supply these, along with other request data to the page to be rendered."
  [data]
  (let [req (:request data)
        req-method (:request-method req)
        route-data (get-in req [:reitit.core/match :data req-method])
        view-fn (:view-fn route-data)
        errors (:humanized (reitit-coercion/encode-error data))]
    (view-fn (assoc req :form-errors errors))))

(defn wrap-request-coercion-exceptions
  "Catch exceptions thrown when coercing request parameters into schemas"
  [handler]
  (fn [req]
    (try
      (handler req)
      (catch ExceptionInfo ex
        (if (= (:type (ex-data ex)) :reitit.coercion/request-coercion)
          (handle-request-coercion-exception (ex-data ex))
          (throw ex))))))

(def api-routes-middleware
  "The middleware listed here will be applied to the API routes."
  [swagger/swagger-feature                    ;; swagger feature
   parameters/parameters-middleware           ;; query-params & form-params
   muuntaja/format-negotiate-middleware       ;; content-negotiation
   muuntaja/format-response-middleware        ;; encoding response body
   ring-coercion/coerce-exceptions-middleware ;; handle coercion exceptions
   muuntaja/format-request-middleware         ;; decoding request body
   ring-coercion/coerce-response-middleware   ;; coercing response bodys
   ring-coercion/coerce-request-middleware    ;; coercing request parameters
   multipart/multipart-middleware             ;; multipart
   db])                                       ;; provide a database transaction

(def web-routes-middleware
  "The middleware listed here will be applied to the the web routes."
  [wrap-csrf
   parameters/parameters-middleware           ;; query-params & form-params
   muuntaja/format-negotiate-middleware       ;; content-negotiation
   muuntaja/format-response-middleware        ;; encoding response body
   db                                         ;; provide a database transaction
   wrap-request-coercion-exceptions           ;; handle coercion exceptions
   muuntaja/format-request-middleware         ;; decoding request body
   ring-coercion/coerce-request-middleware    ;; coercing request parameters
   ring-coercion/coerce-response-middleware]) ;; coercing response bodys

(defn handler-middleware
  "The middleware to be applied to the ring handler.

  The middleware in the vector returned is applied to the request from
  bottom to top. This means the first item in the vector (the outermost
  layer of the wrapping) will be the first to process a request and
  be the last to touch the response."
  [profile db]
  (-> [[(wrap-exceptions profile)]         ;; Handle any exceptions gracefully
       [gzip/wrap-gzip]                    ;; Compress the response
       [(wrap-security-headers profile)]   ;; Add security headers
       [(wrap-ring-defaults db)]           ;; Apply industry standard defaults
       [wrap-flash]]                       ;; Enable flash sessions
      (cond-> (= profile :dev)
        (concat [[wrap-prone]              ;; Present exceptions nicely in the browser
                 [selmer/wrap-error-page]  ;; Present HTML template errors nicely
                 [wrap-reload]]))))        ;; Reload Clojure code when changed
