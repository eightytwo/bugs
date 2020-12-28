(ns bugs.core
  (:require [bugs.bugs.controllers :as bugs-controllers]
            [bugs.bugs.schemas :as bugs-schemas]
            [bugs.layout :as layout]
            [bugs.middleware :as middleware]
            [muuntaja.core :as m]
            [reitit.ring :as ring]
            [reitit.coercion.spec]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.dev.pretty :as pretty]
            [reitit.ring.middleware.dev :as ring-middleware]))

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
    {:no-doc true
     :middleware [middleware/wrap-csrf]}

    ["/"
     {:get {:summary "The homepage"
            :handler (fn [req] (layout/render req "index.html" {:data "world"}))}}]

    ["/bugs"
     {:get {:summary "Display your bugs"
            :handler (fn [req]
                       (layout/render
                        req
                        "bugs.html"
                        {:data (:body (bugs-controllers/get-bugs req))}))}}]]])

(defn create-app [profile db]
  (ring/ring-handler
   (ring/router routes
                {:exception pretty/exception
                 :data      {:db         db
                             :coercion   reitit.coercion.spec/coercion
                             :muuntaja   m/instance
                             :middleware middleware/route-middleware}
                 :reitit.middleware/transform
                 (if (= profile :dev)
                   ring-middleware/print-request-diffs
                   identity)})
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path   "/api-docs"
      :config {:validatorUrl     nil
               :operationsSorter "alpha"}})
    (ring/redirect-trailing-slash-handler)
    (ring/create-default-handler))
   {:middleware (middleware/handler-middleware profile)}))
