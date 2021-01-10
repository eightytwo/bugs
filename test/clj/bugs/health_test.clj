(ns bugs.health-test
  (:require [bugs.setup-test :refer [client start-server-and-db]]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [ragtime.jdbc :as jdbc]))

(use-fixtures :once start-server-and-db)

(deftest get-health
  (let [response (client :get "/api/health")
        commit (->>
                 (shell/sh "git" "rev-parse" "HEAD")
                 :out
                 str/trim-newline)
        last-migration (-> (jdbc/load-resources "migrations") last :id)
        migration-hash (last (str/split last-migration #"-"))]
    (is (= 200 (:status response)))
    (is (= (:body response) {:apiVersion commit :dbVersion migration-hash}))))

