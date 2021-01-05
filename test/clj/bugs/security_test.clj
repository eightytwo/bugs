(ns bugs.security-test
  (:require [bugs.setup-test :refer [client start-server-and-db]]
            [clojure.test :refer :all]))

(use-fixtures :once start-server-and-db)

(deftest check-headers
  (let [response (client :get "/")
        status (:status response)
        headers (:headers response)]
    (is (= 200 status))
    (is (= (headers "Referrer-Policy") "strict-origin"))
    (is (= (headers "X-Content-Type-Options") "nosniff"))
    (is (= (headers "X-XSS-Protection") "1; mode=block"))
    (is (= (headers "X-Frame-Options") "DENY"))
    (is (= (headers "Content-Security-Policy") "default-src 'none'; base-uri 'self'; connect-src 'self'; form-action 'self'; frame-ancestors 'none'; img-src 'self'; style-src 'self';"))))
