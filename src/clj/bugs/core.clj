(ns bugs.core
  (:require [bugs.bugs.controllers :as bugs-controllers]
            [bugs.bugs.schemas :as bugs-schemas]
            [bugs.middleware :as middleware]
            [muuntaja.core :as m]
            [reitit.ring :as ring]
            [reitit.coercion.spec]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.ring.coercion :as coercion]
            [reitit.dev.pretty :as pretty]
            ; [reitit.ring.middleware.dev :as dev-middleware]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]))

(def routes
  [["/swagger.json"
    {:get {:no-doc  true
           :swagger {:info {:title       "Bugs API"
                            :description "The HTTP API for the Bugs application"}}
           :handler (swagger/create-swagger-handler)}}]

   ["/api"
    ["/bugs"
     {:swagger {:tags ["bugs"]}}

     [""
      {:get  {:summary   "Retrieve all of your bugs"
              :responses {200 bugs-schemas/get-bugs-response}
              :handler   bugs-controllers/get-bugs}

       :post {:summary    "Add a bug to your collection"
              :parameters bugs-schemas/post-bugs-request
              :responses  {200 bugs-schemas/post-bugs-response}
              :handler    bugs-controllers/post-bugs}}]

     ["/:id"
      {:get {:summary    "Get a bug by its id"
             :parameters bugs-schemas/get-bug-request
             :responses  {200 bugs-schemas/get-bug-response}
             :handler    bugs-controllers/get-bug}}]]]
   [""
    {:no-doc true}

    ["/"
     {:get {:summary "The homepage"
            :handler (fn [_]
                       {:status  200
                        :headers {"Content-Type" "text/html"}
                        :body    "<h1>Hello, world!</h1>"})}}]
    ["/bugs"
     {:get {:summary "Display your bugs"
            :handler (fn [_]
                       {:status  200
                        :headers {"Content-Type" "text/html"}
                        :body    "<h1>All of your bugs!</h1>"})}}]]])

(def exception-middleware
  (exception/create-exception-middleware
   (merge
    exception/default-handlers
    {;; print stack-traces for all exceptions
     ::exception/wrap (fn [handler e request]
                          ;; TODO: better exception handling
                        (println e)
                        (handler e request))})))

(defn create-app [db]
  (ring/ring-handler
   (ring/router routes
                {:exception pretty/exception
                 :data      {:db         db
                             :coercion   reitit.coercion.spec/coercion
                             :muuntaja   m/instance
                             :middleware [;; swagger feature
                                          swagger/swagger-feature
                                          ;; query-params & form-params
                                          parameters/parameters-middleware
                                          ;; content-negotiation
                                          muuntaja/format-negotiate-middleware
                                          ;; encoding response body
                                          muuntaja/format-response-middleware
                                          ;; exception handling
                                          exception-middleware
                                          ;; decoding request body
                                          muuntaja/format-request-middleware
                                          ;; coercing response bodys
                                          coercion/coerce-response-middleware
                                          ;; coercing request parameters
                                          coercion/coerce-request-middleware
                                          ;; multipart
                                          multipart/multipart-middleware
                                          ;; inject the database into the handler
                                          middleware/db]}
                  ;:reitit.middleware/transform dev-middleware/print-request-diffs
})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path   "/api-docs"
      :config {:validatorUrl     nil
               :operationsSorter "alpha"}})
    (ring/redirect-trailing-slash-handler)
    (ring/create-default-handler))
   {:middleware [[middleware/api-subdomain-to-path :api-subdomain-to-path]]}))
