(ns bugs.system
  (:require [aero.core :as aero]
            [bugs.core :as core]
            [clojure.java.io :as io]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]
            [integrant.core :as ig]
            [next.jdbc.connection :as connection]
            [ring.adapter.jetty :as jetty]
            [selmer.parser :as selmer])
  (:import  (com.mchange.v2.c3p0 ComboPooledDataSource)))

(defmethod aero/reader 'ig/ref
  [_ _ value]
  (ig/ref value))

(defn config
  [profile]
  (aero/read-config (io/resource "system.edn") {:profile profile}))

(defn prep [profile]
  (let [config (config profile)]
    (ig/load-namespaces config)
    config))

(defmethod ig/init-key :bugs/jetty [_ {:keys [handler port]}]
  (println (str "\nServer running on port " port))
  (jetty/run-jetty handler {:port port :join? false}))

(defmethod ig/init-key :bugs/handler [_ {:keys [profile db]}]
  (core/create-app profile db))

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
  [profile]
  (-> (prep profile) ig/prep ig/init))

(comment
  (-main :dev))
