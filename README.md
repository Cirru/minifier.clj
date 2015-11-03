# Cirru minifier

> This project is experimental.

A Clojure library designed to minify Cirru code.

Code generated with [lein-cirru-sepal](https://github.com/Cirru/lein-cirru-sepal).

## Usage

```clojure
(ns your.project
  (:require [cirru.minifier :as minifier]))

(minifier/parse "code in cirru")
```

```
user=> (cirru.minifier/stringify (list (list "a" "b" (list "c")) (list "d")))
"a b (c)\nd"
user=> (:value (cirru.minifier/parse "a b (c)\nd"))
(("a" "b" ("c")) ("d"))
```

* `stringify` generate minified Cirru code
* `parse` parse minified Cirru code

## License

Copyright © 2015 jiyinyiyong

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
