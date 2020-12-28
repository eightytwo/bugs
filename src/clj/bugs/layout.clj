(ns bugs.layout
  (:require [clojure.java.io]
            [selmer.parser :as parser]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.util.response]))

(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))

(defn render
  "renders the HTML template located relative to resources/html"
  [_request template & [params]]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (parser/render-file
          template
          (assoc params
                 :page template
                 :csrf-token *anti-forgery-token*))})

(defn error-page
  [error]
  {:status  (:status error)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (parser/render-file "error.html" error)})
