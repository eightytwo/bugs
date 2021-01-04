(ns bugs.core-test
  (:require [bugs.setup-test :refer [client start-server-and-db]]
            [clojure.string :as str]
            [clojure.test :refer :all]))

(use-fixtures :once start-server-and-db)

(deftest load-homepage
 (let [response (client :get "/")]
   (is (= 200 (:status response)))
   (is (str/includes? (:body response) "<h1>Welcome to Bugs!</h1>"))))

(deftest load-api-docs
  (let [response (client :get "/api/api-docs/")]
    (is (str/includes? (:body response) "<title>Swagger UI</title>"))))

(deftest test-404
 (testing "website 404"
  (let [response (client :get "/this-does-not-exist")]
    (is (= 404 (:status response)))))

 (testing "api 404"
  (let [response (client :get "/api/this-does-not-exist")]
    (is (= 404 (:status response))))))
