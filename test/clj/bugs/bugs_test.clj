(ns bugs.bugs-test
  (:require [bugs.setup-test :refer [client start-server-and-db]]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [hickory.core :as h]
            [hickory.select :as s]
            [jsonista.core :as j]))

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
      (let [bug (first body)
            bug-id (:id bug)
            fetched-bug (:body (client :get (str "/api/bugs/" bug-id)))]
        (is (pos? bug-id))
        (is (contains? bug :createdAt))
        (is (= (dissoc bug :id :createdAt)
               {:name             "Spider"
                :shortDescription "A garden spider"
                :tag              "caution"
                :age              2
                :rating           6}))
        (is (= bug fetched-bug))))))

(deftest add-bug-without-anti-forgery-token
  (let [bug {:name "Spider"}
        response (client :post "/bugs" bug)]
    (is (= 403 (:status response)))))

(deftest add-bug-with-invalid-rating
  (testing "website"
    (binding [clj-http.core/*cookie-store* (clj-http.cookies/cookie-store)]
      (let [bug {:name              "Moth"
                 :short-description "A small moth"
                 :tag               "safe"
                 :age               4
                 :rating            "x"}
            page (-> (client :get "/bugs") :body h/parse h/as-hickory)
            csrf-element (s/select (s/id :__anti-forgery-token) page)
            csrf-token (-> csrf-element first :attrs :value)
            req-body (assoc bug :__anti-forgery-token csrf-token)
            response (client :post "/bugs" {:body req-body})
            body (:body response)]
        (is (= (:status response) 200))
        (is (str/includes? body "<span>should be an integer</span>")))))

  (testing "api"
    (let [bug {:name             "Moth"
               :shortDescription "A small moth"
               :tag              "safe"
               :age              4
               :rating           "x"}
          response (client :post "/api/bugs" {:body bug})
          content-type (get-in response [:headers "Content-Type"])
          body (j/read-value (:body response) j/keyword-keys-object-mapper)]
      (is (= (:status response) 400))
      (is (= content-type "application/json; charset=utf-8"))
      (is (= (-> body :humanized :rating first) "should be an integer")))))
