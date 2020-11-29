(ns user
  (:require [bugs.system :as system]
            [integrant.repl :as ig-repl]
            [ragtime.jdbc :as jdbc]
            [ragtime.repl :as rt-repl]))

(def config (system/read-config "resources/system.edn"))

(ig-repl/set-prep!
  (fn [] config))

(def go ig-repl/go)
(def halt ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(def ragtime-config
  {:datastore  (jdbc/sql-database (:bugs/db config))
   :migrations (jdbc/load-resources "migrations")})

(comment
  (rt-repl/migrate ragtime-config)
  (rt-repl/rollback ragtime-config))

(comment
  (go)
  (halt)
  (reset)
  (reset-all))
