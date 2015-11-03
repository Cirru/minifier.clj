
ns cirru.minifier.parse
  :require
    [] clojure.string :as string

def open-paren "|("
def close-paren "|)"
def whitespace "| "
def line-break "|\n"
def double-quote "|\""
def backslash "|\\"
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
  println state counter
  let
      result $ parser state
    if (:failed result)
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
        assoc result :failed false :msg "|recorvered in not"
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

-- "generators"

defn generate-char (x)
  fn (state)
    if
      > (count (:code state)) 0
      if (match-first state x)
        assoc state
          , :code $ subs (:code state) 1
          , :value x
        fail state "|failed matching character"
      fail state "|error eof"

defn generate-char-in (xs)
  fn (state)
    if
      > (count (:code state)) 0
      if
        >= (.indexOf xs $ subs (:code state) 0 1) 0
        assoc state
          , :code $ subs (:code state) 1
          , :value $ subs (:code state) 0 1
        fail
          assoc state
            , :code $ subs (:code state) 1
            , :value $ subs (:code state) 0 1
          , "|not in char list"
      fail state "|error eof"

-- "parsers"

declare parse-line

defn parse-eof (state)
  fn (state)
    if (= (:code state) |)
      assoc state :value nil
      fail state "|expected eof"

def parse-open-paren $ generate-char open-paren
def parse-close-paren $ generate-char close-paren
def parse-double-quote $ generate-char double-quote
def parse-whitespace $ generate-char whitespace
def parse-backslash $ generate-char backslash
def parse-line-break $ generate-char line-break

defn parse-escaped-char (state)
  if (= (:code state) |)
    fail state :msg "|error eof"
    cond
      (match-first state "|n")
        assoc state :value "|\n" :code (subs (:code state) 2)
      (match-first state "|t")
        assoc state :value "|\t" :code (subs (:code state) 2)
      (match-first state "|\"")
        assoc state :value "|\"" :code (subs (:code state) 2)
      (match-first state "|\\")
        assoc state :value "|\\" :code (subs (:code state) 2)
      :else $ fail state "|no escaped character"

def parse-blanks
  combine-value
    combine-many parse-whitespace
    fn (value is-failed)
      if is-failed value (string/join | value)

def parse-newlines
  combine-value
    combine-many parse-line-break
    fn (value is-failed)
      if is-failed value (string/join | value)

def parse-token-special $ generate-char-in specials-in-token

def parse-string-special $ generate-char-in specials-in-string

def parse-token-end
  combine-peek $ combine-or
    , parse-whitespace parse-close-paren parse-newlines parse-eof

def parse-in-token-char
  combine-not parse-token-special

def parse-in-string-char
  combine-or (combine-not parse-string-special) parse-escaped-char

def parse-token
  combine-chain
    combine-many parse-in-token-char
    , parse-token-end

def parse-string
  combine-chain parse-double-quote
    combine-many parse-in-string-char
    , parse-double-quote

def parse-expression
  combine-chain parse-open-paren parse-line parse-close-paren

def parse-atom
  combine-or parse-expression parse-token parse-string

def parse-line
  combine-alternate parse-atom parse-blanks

def parse-program
  combine-chain
    combine-alternate parse-newlines parse-line
    , parse-eof

def parse
  parse-program initial-state
