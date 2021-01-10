(ns bugs.health.controllers
  (:require [bugs.db :as queries]
            [clojure.java.shell :as shell]
            [clojure.string :as str]))

(defn get-health
  [req]
  (let [db (:db req)
        migration-id (:id (queries/get-latest-migration db))
        migration-hash (last (str/split migration-id #"-"))
        commit (->>
                 (shell/sh "git" "rev-parse" "HEAD")
                 :out
                 str/trim-newline)]
    {:api-version commit
     :db-version  migration-hash}))
