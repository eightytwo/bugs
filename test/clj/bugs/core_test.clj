(ns bugs.core-test
  (:require [clojure.test :refer [deftest testing is]]))

(deftest check-greeting
  (testing "FIXME, I fail."
    (is (= "Hello, world!" (str "Hello," " world!")))))
