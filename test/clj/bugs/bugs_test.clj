(ns bugs.bugs-test
  (:require [bugs.setup-test :refer [client start-server-and-db]]
            [clojure.string :as str]
            [clojure.test :refer :all]))

(use-fixtures :once start-server-and-db)

(deftest get-bugs-empty-database
  (testing "website get bugs"
    (let [response (client :get "/bugs")
          body (:body response)]
      (is (not (str/includes? body "<td>")))))

  (testing "api get bugs"
    (let [response (client :get "/api/bugs")
          content-type (get-in response [:headers "Content-Type"])
          body (:body response)]
      (is (= content-type "application/json; charset=utf-8"))
      (is (= body [])))))

(deftest add-bug-without-anti-forgery-token
  (let [bug {:name "Spider"}
        response (client :post "/bugs" bug)]
    (is (= 403 (:status response)))))
