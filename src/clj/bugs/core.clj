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
  [["/api"
    {:coercion   reitit.coercion.spec/coercion
     :muuntaja   m/instance
     :swagger    {:id ::api}
     :middleware middleware/api-routes-middleware}

    [""
     {:no-doc  true
      :swagger {:info {:title       "Bugs API"
                       :description "The HTTP API for the Bugs application"}}}

     ["/swagger.json"
      {:get (swagger/create-swagger-handler)}]

     ["/docs/*"
      {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"
              :config {:validator-url nil}})}]]

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
     :middleware middleware/web-routes-middleware}

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
                 :data      {:db db}
                 :reitit.middleware/transform
                 (if (= profile :dev)
                   ring-middleware/print-request-diffs
                   identity)})
   (ring/routes
    (ring/redirect-trailing-slash-handler)
    (ring/create-default-handler))
   {:middleware (middleware/handler-middleware profile)}))
