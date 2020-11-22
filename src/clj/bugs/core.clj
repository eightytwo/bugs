(ns bugs.core
  (:require [bugs.math.controllers :as math-controllers]
            [bugs.math.schemas :as math-schemas]
            [bugs.middleware :as middleware]
            [muuntaja.core :as m]
            [reitit.ring :as ring]
            [reitit.coercion.spec]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.ring.coercion :as coercion]
            [reitit.dev.pretty :as pretty]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]))

(def routes
  [["/swagger.json"
    {:get {:no-doc  true
           :swagger {:info {:title       "my-api"
                            :description "with reitit-ring"}}
           :handler (swagger/create-swagger-handler)}}]

   ["/math"
    {:swagger {:tags ["math"]}}

    ["/plus"
     {:get  {:summary    "plus with spec query parameters"
             :parameters math-schemas/get-add-request
             :responses  {200 math-schemas/add-response}
             :handler    math-controllers/get-add}

      :post {:summary    "plus with spec body parameters"
             :parameters math-schemas/post-add-request
             :responses  {200 math-schemas/add-response}
             :handler    math-controllers/post-add}}]]])

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
                                          exception/exception-middleware
                                           ;; decoding request body
                                          muuntaja/format-request-middleware
                                           ;; coercing response bodys
                                          coercion/coerce-response-middleware
                                           ;; coercing request parameters
                                          coercion/coerce-request-middleware
                                           ;; multipart
                                          multipart/multipart-middleware
                                           ;; inject the database into the handler
                                          middleware/db]}})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path   "/"
      :config {:validatorUrl     nil
               :operationsSorter "alpha"}})
    (ring/create-default-handler))))
