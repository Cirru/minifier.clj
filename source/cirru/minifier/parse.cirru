
ns cirru.minifier.parse
  :require
    [] clojure.string :as string

def open-paren "|("
def close-paren "|)"
def whitespace "| "
def line-break "|\n"
def double-quote "|\""
def specials-in-token "|\"() \n\t"
def specials-in-string "|\"\\\n"

def initial-state $ {}
  :code |
  :value nil
  :failed false
  :msg |initial

-- "utilities"

defn match-first (state character)
  = (subs (:code state) 0 1) character

defn fail (state msg)
  assoc state :failed true :msg msg

-- "helper functions"

defn helper-many (state parser counter)
  let
      result $ parser state
    if (:failed state)
      if (> counter 0) state
        fail state "|matching 0 times"
      recur
        assoc result :value
          conj (into ([]) (:value state)) (:value result)
        , parser (+ counter 1)

defn helper-chain (state parsers)
  if (> (count parsers) 0)
    let
        parser $ first parsers
        result $ parser state
      if (:failed result)
        fail state "|failed apply chaining"
        recur
          assoc result :value
            conj (into ([]) (:value state)) (:value result)
          rest parsers
    , state

defn helper-alternate (state parser-1 parser-2 counter)
  let
      result $ parser-1 state
    if (:failed result)
      if (> counter 0) state
        fail state "|not matching alternate rule"
      recur
        assoc result :value
          conj (into ([]) (:value state)) (:value result)
        , parser-2 parser-1 (+ counter 1)

defn helper-or (state parsers)
  if (> (count parsers) 0)
    let
        parser $ first parsers
        result $ parser state
      if (:failed result)
        recur state $ rest parsers
        , result
    fail state "|no parser is successful"

-- "combining functions"

defn combine-many (parser)
  fn (state)
    helper-many (assoc state :value (list)) parser 0

defn combine-chain (& parsers)
  fn (state)
    helper-chain (assoc state :value ([])) parsers

defn combine-alternate (parser-1 parser-2)
  fn (state)
    helper-alternate
      assoc state :value (list)
      , parser-1 parser-2 0

defn combine-or (& parsers)
  fn (state)
    helper-or state parsers

defn combine-not (parser)
  fn (state)
    let
        result (parser state)
      if (:failed result)
        assoc result :failed :msg "|recorvered in not"
        fail result "|should not be this"

defn combine-value (parser handler)
  fn (state)
    let
        result $ parser state
      assoc result :value
        handler (:value result) (:failed result)

defn combine-peek (parser)
  fn (state)
    let
        result $ parser state
      if (:failed result)
        fail state "|peek fail"
        , state

-- "parsers"

declare parse-line

defn parse-eof (state)

defn parse-open-paren (state)
defn parse-close-paren (state)
defn parse-double-quote (state)
defn parse-whitespace (state)
defn parse-backslash (state)
defn parse-line-break (state)

defn parse-escaped-char (state)

defn parse-blanks (state)
  combine-many parse-blanks

defn parse-newlines (state)
  combine-many parse-line-break

defn parse-escape (state)
  combine-chain parse-backslash parse-escaped-char

defn parse-token-special (state)

defn parse-string-special (state)

defn parse-token-end (state)
  combine-peek $ combine-or
    , parse-whitespace parse-close-paren parse-newlines parse-eof

defn parse-in-token-char (state)
  combine-not parse-token-special

defn parse-in-string-char (state)
  combine-or (combine-not parse-string-special) parse-escape

defn parse-token (state)
  combine-chain
    combine-many parse-in-token-char
    , parse-token-end

defn parse-string (state)
  combine-chain parse-double-quote
    combine-many parse-in-string-char
    , parse-double-quote

defn parse-expression (state)
  combine-chain parse-open-paren parse-line parse-close-paren

defn parse-atom (state)
  combine-or parse-expression parse-token parse-string

defn parse-line (state)
  combine-alternate parse-atom parse-blanks

defn parse-program (state)
  combine-chain
    combine-alternate parse-newlines parse-line
    , parse-eof

defn parse (code)
  parse-program initial-state
