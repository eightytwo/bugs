(ns bugs.routes
  (:require [bugs.bugs.routes :as bugs-routes]
            [bugs.health.routes :as health-routes]
            [bugs.layout :as layout]
            [bugs.middleware :as middleware]
            [camel-snake-kebab.core :as csk]
            [muuntaja.core :as m]
            [reitit.coercion.malli :as malli]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]))

(def muuntaja
  "Create a muuntaja instance that presents the API with camelCase keys
  but allows kebab-case keys to be used in Clojure-land."
  (m/create
   (update-in
    m/default-options
    [:formats "application/json"]
    merge
    {:encoder-opts {:encode-key-fn csk/->camelCaseString}
     :decoder-opts {:decode-key-fn csk/->kebab-case-keyword}})))

(def routes
  [["/api"
    {:coercion   (malli/create {:error-keys #{:errors}})
     :muuntaja   muuntaja
     :swagger    {:id ::api}
     :middleware middleware/api-routes-middleware}

    [""
     {:no-doc  true
      :swagger {:info {:title       "Bugs API"
                       :description "The HTTP API for the Bugs application"}}}

     ["/swagger.json"
      {:get (swagger/create-swagger-handler)}]

     ["/api-docs/*"
      {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"
              :config {:validator-url nil}})}]]

    bugs-routes/api-routes
    health-routes/routes]

   [""
    {:coercion   (malli/create {:error-keys #{:errors}})
     :muuntaja   m/instance
     :middleware middleware/web-routes-middleware}

    ["/"
     {:get {:summary "The homepage"
            :handler (fn [_]
                       (layout/render "index.html" {:data "world"}))}}]

    bugs-routes/web-routes]])
