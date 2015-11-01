
(clojure.core/ns cirru.minifier.parse
  (:require [clojure.string :as string]))

(def open-paren "(")

(def close-paren ")")

(def whitespace " ")

(def line-break "\n")

(def double-quote "\"")

(def backslash "\\")

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

(clojure.core/defn generate-char [x]
  (clojure.core/fn [state]
    (if (> (count (:code state)) 0)
      (if (match-first state x)
        (assoc state :code (subs (:code state) 1) :value nil)
        (fail state "failed matching character"))
      (fail state "error eof"))))

(clojure.core/defn generate-char-in [xs]
  (clojure.core/fn [state]
    (if (> (count (:code state)) 0)
      (if (>= (.indexOf xs (subs (:code state) 0 1)))
        (assoc
          state
          :code
          (subs (:code state) 1)
          :value
          (subs (:code state) 0 1))
        (fail state "failed matching character"))
      (fail state "error eof"))))

nil

(declare parse-line)

(clojure.core/defn parse-eof [state]
  (clojure.core/fn [state]
    (if (= (:code state) "")
      (assoc state :value nil)
      (fail state "expected eof"))))

(def parse-open-paren (generate-char open-paren))

(def parse-close-paren (generate-char close-paren))

(def parse-double-quote (generate-char double-quote))

(def parse-whitespace (generate-char whitespace))

(def parse-backslash (generate-char backslash))

(def parse-line-break (generate-char line-break))

(clojure.core/defn parse-escaped-char [state]
  (if (= (:code state) "")
    (fail state :msg "error eof")
    (clojure.core/cond
      (match-first state "n") (assoc :value "\n")
      (match-first state "t") (assoc :value "\t")
      (match-first state "\"") (assoc :value "\"")
      (match-first state "\\") (assoc :value "\\")
      :else (fail state "no escaped character"))))

(def parse-blanks (combine-many parse-blanks))

(def parse-newlines (combine-many parse-line-break))

(def parse-escape (combine-chain parse-backslash parse-escaped-char))

(def parse-token-special (generate-char-in specials-in-token))

(def parse-string-special (generate-char-in specials-in-string))

(def parse-token-end
 (combine-peek
   (combine-or
     parse-whitespace
     parse-close-paren
     parse-newlines
     parse-eof)))

(def parse-in-token-char (combine-not parse-token-special))

(def parse-in-string-char
 (combine-or (combine-not parse-string-special) parse-escape))

(def parse-token
 (combine-chain (combine-many parse-in-token-char) parse-token-end))

(def parse-string
 (combine-chain
   parse-double-quote
   (combine-many parse-in-string-char)
   parse-double-quote))

(def parse-expression
 (combine-chain parse-open-paren parse-line parse-close-paren))

(def parse-atom (combine-or parse-expression parse-token parse-string))

(def parse-line (combine-alternate parse-atom parse-blanks))

(def parse-program
 (combine-chain
   (combine-alternate parse-newlines parse-line)
   parse-eof))

(def parse (parse-program initial-state))