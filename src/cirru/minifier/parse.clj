
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
    (if (:failed result)
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
        (assoc result :failed false :msg "recorvered in not")
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
        (assoc state :code (subs (:code state) 1) :value x)
        (fail
          (assoc state :code (subs (:code state) 1) :value x)
          "failed matching character"))
      (fail state "error eof"))))

(clojure.core/defn generate-char-in [xs]
  (clojure.core/fn [state]
    (if (> (count (:code state)) 0)
      (if (>= (.indexOf xs (subs (:code state) 0 1)) 0)
        (assoc
          state
          :code
          (subs (:code state) 1)
          :value
          (subs (:code state) 0 1))
        (fail
          (assoc
            state
            :code
            (subs (:code state) 1)
            :value
            (subs (:code state) 0 1))
          "not in char list"))
      (fail state "error eof"))))

nil

(declare parse-line)

(clojure.core/defn parse-eof [state]
  (if (= (:code state) "")
    (assoc state :value nil)
    (fail state "expected eof")))

(def parse-open-paren (generate-char open-paren))

(def parse-close-paren (generate-char close-paren))

(def parse-double-quote (generate-char double-quote))

(def parse-whitespace (generate-char whitespace))

(def parse-backslash (generate-char backslash))

(def parse-line-break (generate-char line-break))

(clojure.core/defn parse-escaped-char [state]
  (if (< (count (:code state)) 2)
    (fail state "error eof")
    (clojure.core/cond
      (match-first state "n") (assoc
                                state
                                :value
                                "\n"
                                :code
                                (subs (:code state) 2))
      (match-first state "t") (assoc
                                state
                                :value
                                "\t"
                                :code
                                (subs (:code state) 2))
      (match-first state "\"") (assoc
                                 state
                                 :value
                                 "\""
                                 :code
                                 (subs (:code state) 2))
      (match-first state "\\") (assoc
                                 state
                                 :value
                                 "\\"
                                 :code
                                 (subs (:code state) 2))
      :else (assoc
              state
              :failed
              true
              :value
              (subs (:code state) 0 1)
              :code
              (subs (:code state) 1)
              :msg
              "no escaped character"))))

(def parse-blanks
 (combine-value
   (combine-many parse-whitespace)
   (clojure.core/fn [value is-failed] nil)))

(def parse-newlines
 (combine-value
   (combine-many parse-line-break)
   (clojure.core/fn [value is-failed] nil)))

(def parse-token-special (generate-char-in specials-in-token))

(def parse-string-special (generate-char-in specials-in-string))

(def parse-token-end
 (combine-peek
   (combine-or
     parse-whitespace
     parse-close-paren
     parse-newlines
     parse-eof)))

(clojure.core/defn parse-in-token-char [state]
  (if (= (:code state) "")
    (fail state "error eof")
    ((combine-not parse-token-special) state)))

(clojure.core/defn parse-in-string-char [state]
  (if (= (:code state) "")
    (fail state "error eof")
    (clojure.core/let [parser (combine-or
                                (combine-not parse-string-special)
                                parse-escaped-char)]
      (parser state))))

(def parse-token
 (combine-value
   (combine-chain
     (combine-value
       (combine-many parse-in-token-char)
       (clojure.core/fn [value is-failed]
         (if is-failed nil (string/join "" value))))
     (combine-value
       parse-token-end
       (clojure.core/fn [value is-failed] nil)))
   (clojure.core/fn [value is-failed]
     (if is-failed nil (first value)))))

(def parse-string
 (combine-value
   (combine-chain
     parse-double-quote
     (combine-value
       (combine-many parse-in-string-char)
       (clojure.core/fn [value is-failed]
         (if is-failed nil (string/join "" value))))
     parse-double-quote)
   (clojure.core/fn [value is-failed]
     (if is-failed nil (nth value 1)))))

(clojure.core/defn parse-expression [state]
  (clojure.core/let [parser (combine-value
                              (combine-chain
                                parse-open-paren
                                parse-line
                                parse-close-paren)
                              (clojure.core/fn [value is-failed]
                                (if is-failed nil (nth value 1))))]
    (parser state)))

(clojure.core/defn parse-atom [state]
  (clojure.core/let [parser (combine-or
                              parse-expression
                              parse-token
                              parse-string)]
    (parser state)))

(clojure.core/defn parse-line [state]
  (clojure.core/let [parser (combine-value
                              (combine-alternate
                                parse-atom
                                parse-blanks)
                              (clojure.core/fn [value is-failed]
                                (if
                                  is-failed
                                  nil
                                  (remove nil? value))))]
    (parser state)))

(def parse-program
 (combine-value
   (combine-chain
     (combine-alternate parse-line parse-newlines)
     parse-eof)
   (clojure.core/fn [value is-failed]
     (if is-failed nil (remove nil? (first value))))))

(clojure.core/defn parse [code]
  (parse-program (assoc initial-state :code code)))