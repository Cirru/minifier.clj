
(clojure.core/ns cirru.minify.parse
  (:require [clojure.string :as string]))

(def open-paren "(")

(def close-paren ")")

(def whitespace " ")

(def line-break "\n")

(def double-quote "\"")

(def specials-in-token "\"() \n\t")

(def specials-in-string "\"\\\n")

(def initial-state (clojure.core/hash-map :code "" :value nil))

nil

(clojure.core/defn match-first [state character]
  (= (subs (:code state) 0 1) character))

nil

(clojure.core/defn combine-many [state])

(clojure.core/defn combine-chain [state])

(clojure.core/defn combine-alternate [state])

(clojure.core/defn combine-or [state])

(clojure.core/defn combine-not [state])

nil

(clojure.core/defn parse-eof [state])

(clojure.core/defn parse-open-paren [state])

(clojure.core/defn parse-close-paren [state])

(clojure.core/defn parse-double-quote [state])

(clojure.core/defn parse-any-char [state])

(clojure.core/defn parse-escape [state]
  (combine-chain parse-backslash parse-any-char))

(clojure.core/defn parse-token-special [state])

(clojure.core/defn parse-string-special [state])

(clojure.core/defn parse-in-token-char [state]
  (combine-not parse-token-special))

(clojure.core/defn parse-in-string-char [state]
  (combine-or (combine-not parse-string-special) parse-escape))

(clojure.core/defn parse-token [state]
  (combine-many parse-in-token-char))

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
  (combine-alternate parse-atom parse-space))

(clojure.core/defn parse-program [state]
  (combine-chain
    (combine-alternate parse-newline parse-line)
    parse-eof))

(clojure.core/defn parse [code] (parse-program initial-state))