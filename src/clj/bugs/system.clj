(ns bugs.system
  (:require [environ.core :refer [env]]
            [integrant.core :as ig]
            [next.jdbc :as jdbc]
            [ring.adapter.jetty :as jetty]
            [bugs.core :as core]))

(defn read-config
  [config-file]
  (-> config-file slurp ig/read-string))

(defmethod ig/prep-key :bugs/jetty
  [_ config]
  (if (env :port)
    (merge config {:port (Integer/parseInt (env :port))})
    config))

(defmethod ig/prep-key :bugs/db
  [_ config]
  (merge config {:jdbc-url (env :jdbc-bugs-url)}))

(defmethod ig/init-key :bugs/jetty [_ {:keys [handler port]}]
  (println (str "\nServer running on port " port))
  (jetty/run-jetty handler {:port port :join? false}))

(defmethod ig/init-key :bugs/handler [_ {:keys [db]}]
  (core/create-app db))

(defmethod ig/init-key :bugs/db [_ db]
  (jdbc/get-datasource db))

(defmethod ig/halt-key! :bugs/jetty [_ jetty]
  (.stop jetty))

(defn -main
  [config-file]
  (let [config (read-config config-file)]
    (-> config ig/prep ig/init)))

(comment
  (-main "resources/config.edn"))
