(ns bugs.health.schemas)

(def get-health-response
  [:map
   [:api-version string?
    :db-version  string?]])
