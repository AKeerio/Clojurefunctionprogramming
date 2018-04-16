(ns tasktwo.core
  (:gen-class)
  (:require clojure.test)
  (:require [cheshire.core :refer :all])
)
;Open everything inside this folder
(def files (file-seq (clojure.java.io/file "acme-data")))

;Check if its a file rather than a folder
(defn only-files
  [file-s]
  (filter #(.isFile %) file-s))

;Convert the LazySeq to a vector
(def files-vec (vec (only-files files)))

;Testing on 100 files becuase its taking too long
(def files2 (take 100 files-vec))
(def orders [])
(for [i files2]
    (if (boolean (re-find #"orders.json" (str i)))
      (def orders (concat orders (parse-stream (clojure.java.io/reader i) true)))
    )
)
(print orders)
(println "There are" (count (map :id orders)) "unique products")
(map :delivery-address orders)
