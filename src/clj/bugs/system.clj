(ns bugs.system
  (:require [bugs.core :as core]
            [environ.core :refer [env]]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]
            [integrant.core :as ig]
            [next.jdbc.connection :as connection]
            [ring.adapter.jetty :as jetty]
            [selmer.parser :as selmer])
  (:import  (com.mchange.v2.c3p0 ComboPooledDataSource)))

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
  (hugsql/set-adapter! (next-adapter/hugsql-adapter-next-jdbc))
  (connection/->pool ComboPooledDataSource db))

(defmethod ig/init-key :bugs/selma [_ selma_config]
  (selmer/set-resource-path! (:templates-dir selma_config)))

(defmethod ig/halt-key! :bugs/jetty [_ jetty]
  (.stop jetty))

(defmethod ig/halt-key! :bugs/db [_ db]
  (.close db))

(defn -main
  [config-file]
  (let [config (read-config config-file)]
    (-> config ig/prep ig/init)))

(comment
  (-main "resources/system.edn"))
