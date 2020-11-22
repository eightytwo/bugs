(ns bugs.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [bugs.core :refer [say-hello]]))

(deftest check-greeting
  (testing "FIXME, I fail."
    (is (= "Hello, world!" (say-hello "world")))))
