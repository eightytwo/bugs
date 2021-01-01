(ns user
  (:require [bugs.system :as system]
            [integrant.repl :as ig-repl]
            [integrant.repl.state :as ig-state]
            [ragtime.jdbc :as jdbc]
            [ragtime.repl :as rt-repl]
            hugsql.core))

(ig-repl/set-prep! #(system/prep :dev))

(def config ig-state/config)
(def go ig-repl/go)
(def halt ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(def ragtime-config
  {:datastore  (jdbc/sql-database {:connection-uri (get-in config [:bugs/db :jdbcUrl])})
   :migrations (jdbc/load-resources "migrations")})

(comment
  (rt-repl/migrate ragtime-config)
  (rt-repl/rollback ragtime-config))

(comment
  (go)
  (halt)
  (reset)
  (reset-all))

(comment
  (hugsql.core/def-db-fns "sql/bugs.sql"))
