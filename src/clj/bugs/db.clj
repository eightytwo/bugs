(ns bugs.db
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "sql/bugs.sql")
