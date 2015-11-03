
ns cirru.minifier-test
  :require
    [] clojure.test :refer :all
    [] cirru.minifier :refer :all

def demo-tree $ []
  [] |a |b
  [] |c $ [] |d |e
  [] "|i i"
def demo-code "|a b\nc (d e)\n\"i i\""

deftest stringify-test
  testing "|try stringify simple code"
    is (= (stringify demo-tree) demo-code)

deftest parse-test
  testing "|try parse simple code"
    is (= (:value (parse demo-code)) demo-tree)
