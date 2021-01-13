(ns bugs.setup-test
  (:require [bugs.system :as system]
            [clj-http.client :as http]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [integrant.repl :as ig-repl]
            [integrant.repl.state :as ig-state]
            [jsonista.core :as j]
            [ragtime.jdbc :as jdbc]
            [ragtime.repl :as rt-repl]))

(defn ragtime-config
  [jdbc-url]
  {:datastore  (jdbc/sql-database {:connection-uri jdbc-url})
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
  (let [jdbc-url (get-in ig-state/config [:bugs/db :jdbcUrl])
        config (ragtime-config jdbc-url)
        first-migration (first (:migrations config))]
    (try
      (rt-repl/rollback config (:id first-migration))
      (catch Exception _))
    (rt-repl/migrate config)))

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

(defn full-url
  [path]
  (let [port (get-in ig-state/config [:bugs/http-server :port])]
    (str "http://localhost:" port path)))

(defn client
  ([method path]
   (client method path {}))
  ([method path opts]
   (let [is-api (and (str/starts-with? path "/api")
                     (not (str/includes? path "/api-docs")))
         is-write (contains? #{:post :put} method)
         http-fn (cond
                   (= method :get) http/get
                   (= method :post) http/post)
         all-opts (-> {:throw-exceptions false}
                      (merge opts)
                      (cond-> is-api
                        (merge {:accept :json, :as :json}))
                      (cond-> (and is-api is-write)
                        (merge {:content-type :json,
                                :body (j/write-value-as-string (:body opts))}))
                      (cond-> (and (not is-api) is-write)
                        (merge {:form-params (:body opts)})))]
     (http-fn (full-url path) all-opts))))

(comment
  (run-all-tests))
