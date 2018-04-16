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

(def orders [])
(def purchase-orders [])
(def shipments [])

(for [i files-vec]
    (if (boolean (re-find #"orders.json" (str i)))
      (def orders (concat orders (parse-stream (clojure.java.io/reader i) true)))
      ;(println "Not a order")
    )
)

(def purchase-orders [])
(for [i files-vec]
    (if (boolean (re-find #"purchase-orders.json" (str i)))
      (def purchase-orders (concat purchase-orders (parse-stream (clojure.java.io/reader i) true)))
      ;(println "Not a purchase-order")
    )
)

(def shipments [])
(for [i files-vec]
    (if (boolean (re-find #"shipments.json" (str i)))
      (def shipments (concat shipments (parse-stream (clojure.java.io/reader i) true)))
      ;(println "Not a shipments")
    )
)

(println "There are" (count (map :id orders)) "unique orders")
(println "There are" (count (map :id purchase-orders)) "unique purchase-orders")
(println "There are" (count (map :id shipments)) "unique shipments")
