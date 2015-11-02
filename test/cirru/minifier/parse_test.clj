(ns cirru.minifier.parse-test
  (:require [clojure.test :refer :all]
            [cirru.minifier.parse :as parser]))

(deftest parse-open-paren
  (testing "test open paren"
    (is
      (=
        (parser/parse-open-paren
          (assoc parser/initial-state :code "(a"))
        (assoc parser/initial-state :code "a" :value "(")))))

(deftest parse-whitespace-test
  (testing "test whitespace"
    (is (= 1 1))))
