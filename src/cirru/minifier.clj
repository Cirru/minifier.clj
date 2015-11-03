
(clojure.core/ns cirru.minifier
  (:require [clojure.string :as string]
            [cirru.minifier.stringify :as comp-stringify]
            [cirru.minifier.parse :as comp-parse]))

(def stringify comp-stringify/stringify)

(def parse comp-parse/parse)