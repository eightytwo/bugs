(ns bugs.db
  (:require [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as csk-extras]
            hugsql.adapter
            hugsql.core))

(hugsql.core/def-db-fns "sql/bugs.sql")

(def memoized-kebab-keyword-ulate (memoize csk/->kebab-case-keyword))

(defn result-one-snake->kebab
  [this result options]
  (->> (hugsql.adapter/result-one this result options)
       (csk-extras/transform-keys memoized-kebab-keyword-ulate)))

(defn result-many-snake->kebab
  [this result options]
  (->> (hugsql.adapter/result-many this result options)
       (mapv #(csk-extras/transform-keys memoized-kebab-keyword-ulate %))))

(defn result-affected-snake->kebab
  [this result options]
  (->> (hugsql.adapter/result-affected this result options)
       (mapv #(csk-extras/transform-keys memoized-kebab-keyword-ulate %))))

(defn result-raw-snake->kebab
  [this result options]
  (->> (hugsql.adapter/result-raw this result options)
       (mapv #(csk-extras/transform-keys memoized-kebab-keyword-ulate %))))

(defmethod hugsql.core/hugsql-result-fn :1 [_sym] 'bugs.db/result-one-snake->kebab)
(defmethod hugsql.core/hugsql-result-fn :one [_sym] 'bugs.db/result-one-snake->kebab)
(defmethod hugsql.core/hugsql-result-fn :* [_sym] 'bugs.db/result-many-snake->kebab)
(defmethod hugsql.core/hugsql-result-fn :many [_sym] 'bugs.db/result-many-snake->kebab)
(defmethod hugsql.core/hugsql-result-fn :n [_sym] 'bugs.db/result-affected-snake->kebab)
(defmethod hugsql.core/hugsql-result-fn :affected [_sym] 'bugs.db/result-affected-snake->kebab)
(defmethod hugsql.core/hugsql-result-fn :raw [_sym] 'bugs.db/result-raw-snake->kebab)
(defmethod hugsql.core/hugsql-result-fn :default [_sym] 'bugs.db/result-raw-snake->kebab)
