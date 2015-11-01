
ns cirru.minify.parse
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

-- "utilities"

defn match-first (state character)
  = (subs (:code state) 0 1) character

-- "combining functions"

defn combine-many (state)

defn combine-chain (state)

defn combine-alternate (state)

defn combine-or (state)

defn combine-not (state)

-- "parsers"

defn parse-eof (state)

defn parse-open-paren (state)
defn parse-close-paren (state)
defn parse-double-quote (state)

defn parse-any-char (state)

defn parse-escape (state)
  combine-chain parse-backslash parse-any-char

defn parse-token-special (state)

defn parse-string-special (state)

defn parse-in-token-char (state)
  combine-not parse-token-special

defn parse-in-string-char (state)
  combine-or (combine-not parse-string-special) parse-escape

defn parse-token (state)
  combine-many parse-in-token-char

defn parse-string (state)
  combine-chain parse-double-quote
    combine-many parse-in-string-char
    , parse-double-quote

defn parse-expression (state)
  combine-chain parse-open-paren parse-line parse-close-paren

defn parse-atom (state)
  combine-or parse-expression parse-token parse-string

defn parse-line (state)
  combine-alternate parse-atom parse-space

defn parse-program (state)
  combine-chain
    combine-alternate parse-newline parse-line
    , parse-eof

defn parse (code)
  parse-program initial-state
