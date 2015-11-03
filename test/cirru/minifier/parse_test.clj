
(clojure.core/ns cirru.minifier.parse-test
  (:require [clojure.test :refer :all]
            [cirru.minifier.parse :refer :all]))

(deftest
  parse-open-paren-test
  (testing
    "test open paren"
    (is
      (=
        (parse-open-paren (assoc initial-state :code "(a"))
        (assoc initial-state :code "a" :value "(")))))

(deftest
  parse-close-paren-test
  (testing
    "test close paren"
    (is
      (=
        (parse-close-paren (assoc initial-state :code ")a"))
        (assoc initial-state :code "a" :value ")")))))

(deftest
  parse-double-quote-test
  (testing
    "test double quote"
    (is
      (=
        (parse-double-quote (assoc initial-state :code "\"a"))
        (assoc initial-state :code "a" :value "\"")))))

(deftest
  parse-backslash-test
  (testing
    "test backslash"
    (is
      (=
        (parse-backslash (assoc initial-state :code "\\a"))
        (assoc initial-state :code "a" :value "\\")))))

(deftest
  parse-whitespace-test
  (testing
    "test whitespace"
    (is
      (=
        (parse-whitespace (assoc initial-state :code " a"))
        (assoc initial-state :code "a" :value " ")))))

(deftest
  parse-line-break-test
  (testing
    "test line break"
    (is
      (=
        (parse-line-break (assoc initial-state :code "\na"))
        (assoc initial-state :code "a" :value "\n")))))

(deftest
  parse-escaped-char-test
  (testing
    "test escaped char"
    (is
      (=
        (parse-escaped-char (assoc initial-state :code "\\\\a"))
        (assoc initial-state :code "a" :value "\\")))))

(deftest
  parse-blanks-test
  (testing
    "test blanks"
    (is
      (=
        (parse-blanks (assoc initial-state :code "  a"))
        (assoc initial-state :code "a" :value "  ")))))