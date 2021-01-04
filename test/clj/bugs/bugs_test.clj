(ns bugs.bugs-test
  (:require [bugs.setup-test :as setup]
            [clojure.string :as str]
            [clojure.test :refer :all]))

(use-fixtures :once setup/start-server-and-db)

(deftest load-homepage
  (let [response (setup/request :get "/")]
    (is (= 200 (:status response)))
    (is (str/includes? (:body response) "<h1>Welcome to Bugs!</h1>"))))

(deftest test-404
  (let [response (setup/request :get "/xyz")]
    (is (= 404 (:status response)))))

(deftest get-bugs
 (let [response (setup/request :get "/bugs")
       body     (:body response)]
   (is (not (str/includes? body "<td>")))))
