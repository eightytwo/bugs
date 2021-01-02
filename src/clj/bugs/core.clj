(ns bugs.core
  (:require [bugs.routes :as routes]
            [bugs.middleware :as middleware]
            [reitit.ring :as ring]
            [reitit.dev.pretty :as pretty]
            [reitit.ring.middleware.dev :as ring-middleware]))

(defn create-app [profile db]
  (ring/ring-handler
   (ring/router routes/routes
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
