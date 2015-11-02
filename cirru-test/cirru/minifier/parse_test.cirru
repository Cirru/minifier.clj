
ns cirru.minifier.parse-test
  :require
    [] clojure.test :refer :all
    [] cirru.minifier.parse :as parser

deftest parse-open-paren-test
  testing "|test open paren" $ is $ =
    parser/parse-open-paren
      assoc parser/initial-state :code "|(a"
    assoc parser/initial-state :code |a :value "|("

deftest parse-close-paren-test
  testing "|test close paren" $ is $ =
    parser/parse-close-paren
      assoc parser/initial-state :code "|)a"
    assoc parser/initial-state :code |a :value "|)"

deftest parse-double-quote-test
  testing "|test double quote" $ is $ =
    parser/parse-double-quote
      assoc parser/initial-state :code "|\"a"
    assoc parser/initial-state :code |a :value "|\""

deftest parse-backslash-test
  testing "|test backslash" $ is $ =
    parser/parse-backslash
      assoc parser/initial-state :code "|\\a"
    assoc parser/initial-state :code |a :value "|\\"

deftest parse-whitespace-test
  testing "|test whitespace" $ is $ =
    parser/parse-whitespace
      assoc parser/initial-state :code "| a"
    assoc parser/initial-state :code |a :value "| "

deftest parse-line-break-test
  testing "|test line break" $ is $ =
    parser/parse-line-break
      assoc parser/initial-state :code "|\na"
    assoc parser/initial-state :code |a :value "|\n"

deftest parse-blanks-test
  testing "|test blanks" $ is $ =
    parser/parse-blanks
      assoc parser/initial-state :code "|  a"
    assoc parser/initial-state :code |a :value "|  "
