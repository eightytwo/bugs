(ns bugs.health.routes
  (:require [bugs.health.controllers :as c]
            [bugs.health.schemas :as s]
            [ring.util.http-response :refer [ok]]))

(def routes
  ["/health"
   {:no-doc true
    :get    {:summary   "API health check"
             :responses {200 s/get-health-response}
             :handler   #(ok (c/get-health %))}}])
