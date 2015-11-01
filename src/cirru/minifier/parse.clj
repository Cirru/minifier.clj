
(clojure.core/ns cirru.minifier.parse
  (:require [clojure.string :as string]))

(def open-paren "(")

(def close-paren ")")

(def whitespace " ")

(def line-break "\n")

(def double-quote "\"")

(def specials-in-token "\"() \n\t")

(def specials-in-string "\"\\\n")

(def initial-state
 (clojure.core/hash-map
   :code
   ""
   :value
   nil
   :failed
   false
   :msg
   "initial"))

nil

(clojure.core/defn match-first [state character]
  (= (subs (:code state) 0 1) character))

(clojure.core/defn fail [state msg] (assoc state :failed true :msg msg))

nil

(clojure.core/defn helper-many [state parser counter]
  (clojure.core/let [result (parser state)]
    (if (:failed state)
      (if (> counter 0) state (fail state "matching 0 times"))
      (recur
        (assoc
          result
          :value
          (conj (into [] (:value state)) (:value result)))
        parser
        (+ counter 1)))))

(clojure.core/defn helper-chain [state parsers]
  (if (> (count parsers) 0)
    (clojure.core/let [parser (first parsers) result (parser state)]
      (if (:failed result)
        (fail state "failed apply chaining")
        (recur
          (assoc
            result
            :value
            (conj (into [] (:value state)) (:value result)))
          (rest parsers))))
    state))

(clojure.core/defn helper-alternate [state parser-1 parser-2 counter]
  (clojure.core/let [result (parser-1 state)]
    (if (:failed result)
      (if (> counter 0)
        state
        (fail state "not matching alternate rule"))
      (recur
        (assoc
          result
          :value
          (conj (into [] (:value state)) (:value result)))
        parser-2
        parser-1
        (+ counter 1)))))

(clojure.core/defn helper-or [state parsers]
  (if (> (count parsers) 0)
    (clojure.core/let [parser (first parsers) result (parser state)]
      (if (:failed result) (recur state (rest parsers)) result))
    (fail state "no parser is successful")))

nil

(clojure.core/defn combine-many [parser]
  (clojure.core/fn [state]
    (helper-many (assoc state :value (list)) parser 0)))

(clojure.core/defn combine-chain [& parsers]
  (clojure.core/fn [state]
    (helper-chain (assoc state :value []) parsers)))

(clojure.core/defn combine-alternate [parser-1 parser-2]
  (clojure.core/fn [state]
    (helper-alternate (assoc state :value (list)) parser-1 parser-2 0)))

(clojure.core/defn combine-or [& parsers]
  (clojure.core/fn [state] (helper-or state parsers)))

(clojure.core/defn combine-not [parser]
  (clojure.core/fn [state]
    (clojure.core/let [result (parser state)]
      (if (:failed result)
        (assoc result :failed :msg "recorvered in not")
        (fail result "should not be this")))))

(clojure.core/defn combine-value [parser handler]
  (clojure.core/fn [state]
    (clojure.core/let [result (parser state)]
      (assoc
        result
        :value
        (handler (:value result) (:failed result))))))

(clojure.core/defn combine-peek [parser]
  (clojure.core/fn [state]
    (clojure.core/let [result (parser state)]
      (if (:failed result) (fail state "peek fail") state))))

nil

(declare parse-line)

(clojure.core/defn parse-eof [state])

(clojure.core/defn parse-open-paren [state])

(clojure.core/defn parse-close-paren [state])

(clojure.core/defn parse-double-quote [state])

(clojure.core/defn parse-whitespace [state])

(clojure.core/defn parse-backslash [state])

(clojure.core/defn parse-line-break [state])

(clojure.core/defn parse-escaped-char [state])

(clojure.core/defn parse-blanks [state] (combine-many parse-blanks))

(clojure.core/defn parse-newlines [state]
  (combine-many parse-line-break))

(clojure.core/defn parse-escape [state]
  (combine-chain parse-backslash parse-escaped-char))

(clojure.core/defn parse-token-special [state])

(clojure.core/defn parse-string-special [state])

(clojure.core/defn parse-token-end [state]
  (combine-peek
    (combine-or
      parse-whitespace
      parse-close-paren
      parse-newlines
      parse-eof)))

(clojure.core/defn parse-in-token-char [state]
  (combine-not parse-token-special))

(clojure.core/defn parse-in-string-char [state]
  (combine-or (combine-not parse-string-special) parse-escape))

(clojure.core/defn parse-token [state]
  (combine-chain (combine-many parse-in-token-char) parse-token-end))

(clojure.core/defn parse-string [state]
  (combine-chain
    parse-double-quote
    (combine-many parse-in-string-char)
    parse-double-quote))

(clojure.core/defn parse-expression [state]
  (combine-chain parse-open-paren parse-line parse-close-paren))

(clojure.core/defn parse-atom [state]
  (combine-or parse-expression parse-token parse-string))

(clojure.core/defn parse-line [state]
  (combine-alternate parse-atom parse-blanks))

(clojure.core/defn parse-program [state]
  (combine-chain
    (combine-alternate parse-newlines parse-line)
    parse-eof))

(clojure.core/defn parse [code] (parse-program initial-state))