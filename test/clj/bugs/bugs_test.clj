(ns bugs.bugs-test
  (:require [bugs.setup-test :refer [client start-server-and-db]]
            [clojure.string :as str]
            [clojure.test :refer :all]))

(use-fixtures :once start-server-and-db)

;; This test needs a complete rewrite (into separate tests) but I just
;; wanted to get tests which create and read data via the website and
;; api working.
(deftest get-bugs
  (testing "website get bugs empty database"
    (let [response (client :get "/bugs")
          content-type (get-in response [:headers "Content-Type"])
          body (:body response)]
      (is (= content-type "text/html; charset=utf-8"))
      (is (not (str/includes? body "<td>"))))

    (let [response (client :get "/api/bugs")
          content-type (get-in response [:headers "Content-Type"])
          body (:body response)]
      (is (= content-type "application/json; charset=utf-8"))
      (is (= body [])))

    (let [bug {:name             "Spider"
               :shortDescription "A garden spider"
               :tag              "caution"
               :age              2
               :rating           6}
          response (client :post "/api/bugs" {:body bug})
          body (:body response)]
      (is (= (:status response) 200))
      (is (pos? (:id body)))
      (is (contains? body :createdAt))
      (is (= (dissoc body :id :createdAt) bug)))

    (let [response (client :get "/bugs")
          body (:body response)]
      (is (str/includes? body "<td>Spider</td>")))

    (let [response (client :get "/api/bugs")
          body (:body response)]
      (is (= (count body) 1))
      (let [bug (first body)]
        (is (pos? (:id bug)))
        (is (contains? bug :createdAt))
        (is (= (dissoc bug :id :createdAt)
               {:name             "Spider"
                :shortDescription "A garden spider"
                :tag              "caution"
                :age              2
                :rating           6}))))))

(deftest add-bug-without-anti-forgery-token
  (let [bug {:name "Spider"}
        response (client :post "/bugs" bug)]
    (is (= 403 (:status response)))))
