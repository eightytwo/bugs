(ns user
  (:require [bugs.system :as system]
            [integrant.repl :as ig-repl]
            [integrant.repl.state :as ig-state]
            [ragtime.jdbc :as jdbc]
            [ragtime.repl :as rt-repl]
            hugsql.core))

(ig-repl/set-prep! #(system/prep :dev))

(def go ig-repl/go)
(def halt ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(defn ragtime-config
  []
  (let [jdbc-url (get-in ig-state/config [:bugs/db :jdbcUrl])]
    {:datastore  (jdbc/sql-database {:connection-uri jdbc-url})
     :migrations (jdbc/load-resources "migrations")}))

(comment
  (rt-repl/migrate (ragtime-config))
  (rt-repl/rollback (ragtime-config)))

(comment
  (go)
  (halt)
  (reset)
  (reset-all))

(comment
  (hugsql.core/def-db-fns "sql/bugs.sql"))
