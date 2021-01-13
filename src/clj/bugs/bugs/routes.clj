(ns bugs.bugs.routes
  (:require [bugs.bugs.controllers :as c]
            [bugs.bugs.schemas :as s]
            [ring.util.http-response :refer [ok]]))

(def api-routes
  ["/bugs"
   {:swagger {:tags ["bugs"]}}

   [""
    {:get  {:summary   "Retrieve all of your bugs"
            :responses {200 s/get-bugs-response}
            :handler   #(ok (c/get-bugs %))}

     :post {:summary    "Add a bug to your collection"
            :parameters {:body s/new-bug}
            :responses  {200 s/post-bugs-response}
            :handler    #(ok (c/create-bug %))}}]

   ["/:id"
    {:get {:summary    "Get a bug by its id"
           :parameters s/get-bug-request
           :responses  {200 s/get-bug-response}
           :handler    #(ok (c/get-bug %))}}]])

(def web-routes
  ["/bugs"
   {:get  {:summary "Display your bugs"
           :handler #(c/show-bugs %)}

    :post {:summary    "Add a bug to your collection"
           :parameters {:form s/new-bug}
           :view-fn    c/show-bugs
           :handler    (fn [req]
                         (c/create-bug req)
                         (c/show-bugs req))}}])
