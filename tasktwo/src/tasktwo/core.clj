(ns tasktwo.core
  (:gen-class)
  (:require clojure.test)
  (:require [cheshire.core :refer :all])
)

(def mini-products (parse-stream (clojure.java.io/reader "mini-data\\products.json") true))
(def mini-orders (parse-stream (clojure.java.io/reader "mini-data\\orders.json") true))
(def mini-offers (parse-stream (clojure.java.io/reader "mini-data\\offers.json") true))

(println "There are" (count  (map :id mini-products)) "unique products (mini data)")
(println "There are" (count  (map :id mini-orders)) "unique orders (mini data)")
(println "There are" (count  (map :code mini-offers)) "unique offers (mini data)")

;; Data of SKU-1038 shirt
(doseq [i mini-products]
  (if (= (:name i) (str "Dr. McCoy T-shirt")) (println (i :variants)))
)

;;Price of SKU-1038
(doseq [i mini-products]
  (if (= (get-in i [:variants :SKU-1038 :sku]) (str "SKU-1038"))
    (println "Price of SKU-1038" (get-in i [:variants :SKU-1038 :price :GBP])))
)

(doseq [i mini-products]
  (if (= (:name i) (str "Chekov T-shirt"))
    (dotimes [j (count (vec (nth (nth (vec i) 3) 1)))]
      (if(= "L" (str (get-in shirtdata [:variants (keyword (nth (nth (vec (nth (nth (vec i) 3) 1)) j)0)) :options :size])))
        (println "There are " (count (vec (nth (nth (vec i) 3) 1))) " available sizes of Chekov shirt."
          "\nPrice of large Checkov t-shirt is "
          (get-in shirtdata [:variants (keyword (nth (nth (vec (nth (nth (vec i) 3) 1)) j)0)) :price :GBP])
        )
      )
    )
  )
)





;-------------------------------------------------------------------------------------------------------
;                                             Full data
;-------------------------------------------------------------------------------------------------------
(def products (parse-stream (clojure.java.io/reader "acme-data\\products.json") true))
(def offers  (parse-stream (clojure.java.io/reader "acme-data\\offers.json") true))
;Open everything inside this folder
(def files (file-seq (clojure.java.io/file "acme-data")))
;Check if its a file rather than a folder
(defn only-files
  [file-s]
  (filter #(.isFile %) file-s))
;Convert the LazySeq to a vector
(def files-vec (vec (only-files files)))

;Initialise variables
(def orders [])
(def purchase-orders [])
(def shipments [])

(for [i files-vec]
  (if (boolean (re-find #"\\orders\\" (str i)))
    (def orders (concat orders (parse-stream (clojure.java.io/reader i) true)))
    ;if not a order check its either a shipment or a purchase-order
    (if (boolean (re-find #"\\purchase-orders\\" (str i)))
      (def purchase-orders (concat purchase-orders (parse-stream (clojure.java.io/reader i) true)))
      ;if its not a purchase-order its would be a shipment
      (if (boolean (re-find #"\\shipments\\" (str i)))
        (def shipments (concat shipments (parse-stream (clojure.java.io/reader i) true)))
        (println "No valid files found")
      )
    )
  )
)

(println "There are" (count  (map :id products)) "unique products (full data)")
(println "There are" (count  (map :id purchase-orders)) "unique purchase-orders (full data)")
(println "There are" (count  (map :id orders)) "unique orders (full data)")
(println "There are" (count  (map :id shipments)) "unique shipments (full data)")
