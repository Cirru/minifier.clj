
(clojure.core/ns cirru.minifier.stringify
  (:require [clojure.string :as string] [clojure.pprint :as pprint]))

(declare stringify-expression)

(def left-paren "(")

(def right-paren ")")

(clojure.core/defn stringify-token [token]
  token
  (if (re-find (re-pattern "[\\n\\s$,\"]") token)
    (with-out-str (pprint/write token))
    token))

(clojure.core/defn stringify-x [x]
  (if (string? x) (stringify-token x) (stringify-expression x)))

(clojure.core/defn stringify-expression [expression]
  (str
    left-paren
    (string/join " " (map stringify-x expression))
    right-paren))

(clojure.core/defn stringify [tree]
  (string/join "\n" (map stringify-expression tree)))