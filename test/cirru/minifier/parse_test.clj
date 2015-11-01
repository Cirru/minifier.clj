(ns cirru.minifier.parse-test
  (:require [clojure.test :refer :all]
            [cirru.minifier.parse :refer :all]))

(deftest parse-whitespace-test
  (testing "test whitespace"
    (is (= 1 1))))
