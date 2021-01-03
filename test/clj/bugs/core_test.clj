(ns bugs.core-test
  (:require [bugs.system :as system]
            [clj-http.client :as http]
            [clojure.test :refer :all]
            [integrant.repl :as ig-repl]
            [integrant.repl.state :as ig-state]
            [ragtime.jdbc :as jdbc]
            [ragtime.repl :as rt-repl]))

(def ragtime-config
  {:datastore  (jdbc/sql-database {:connection-uri (get-in ig-state/config [:bugs/db :jdbcUrl])})
   :migrations (jdbc/load-resources "migrations")})

(defn start-system
  []
  (ig-repl/set-prep! #(system/prep :test))
  (ig-repl/go))

(defn stop-system
  []
  (ig-repl/halt))

(defn migrate-db
  []
  (try
    (let [first-migration (first (:migrations ragtime-config))]
      (rt-repl/rollback ragtime-config (:id first-migration)))
    (catch Exception _))
  (rt-repl/migrate ragtime-config))

(defn start-server-and-db
  [f]
  (try
    (start-system)
    (migrate-db)
    (f)
    (catch Exception e
      (clojure.stacktrace/print-stack-trace e))
    (finally
      (stop-system))))

(use-fixtures :once start-server-and-db)

(defonce ^:private base-url
  (str
    "http://localhost:"
    (get-in ig-state/config [:bugs/http-server :port])))

(defn request
  [method path]
  (let [url (str base-url path)
        http-fn (cond
                  (= method :get) http/get
                  (= method :post) http/post)]
    (http-fn url {:throw-exceptions false})))

(deftest test-website
  (testing "home page"
    (let [response (request :get "/")]
      (is (= 200 (:status response)))))

  (testing "not found"
    (let [response (request :get "/xyz")]
      (is (= 404 (:status response))))))
