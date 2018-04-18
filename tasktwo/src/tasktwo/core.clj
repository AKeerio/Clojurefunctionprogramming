(ns tasktwo.core
  (:gen-class)
  (:require clojure.test)
  (:require [cheshire.core :refer :all])
  (require [clj-time.core :as t])
  (require [clj-time.format :as f])
  (require [clj-time.coerce :as c])
  (use hiccup.core)
)
;-------------------------------------------------------------------------------------------------------
;                                             Full data
;-------------------------------------------------------------------------------------------------------
(defn load-mini-data []
  (def mini-products (parse-stream (clojure.java.io/reader "mini-data\\products.json") true))
  (def mini-orders (parse-stream (clojure.java.io/reader "mini-data\\orders.json") true))
  (def mini-offers (parse-stream (clojure.java.io/reader "mini-data\\offers.json") true))
)
(load-mini-data)
;; load all data
(defn load-full-data []
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
          ;(println "No valid files found"
        )
      )
    )
  )
)
(load-full-data)

(defn test-data []
  (println "There are" (count  (map :id products)) "unique products (full data)")
  (println "There are" (count  (map :id purchase-orders)) "unique purchase-orders (full data)")
  (println "There are" (count  (map :id orders)) "unique orders (full data)")
  (println "There are" (count  (map :id shipments)) "unique shipments (full data)")
)
(test-data)

;; Data of a shirt
(do
  (doseq [i products]
    (if (= (:name i) "Dr. McCoy T-shirt") (def shirtdata-by-name (i :variants)))
  )
  (println shirtdata-by-name)
)

;;Price of SKU-1038
(do
  (doseq [i products]
    (if (= (get-in i [:variants (keyword "SKU-1038") :sku]) "SKU-1038")
      (def price-by-sku (get-in i [:variants (keyword "SKU-1038") :price :GBP])))
  )
  (println "Price:" price-by-sku)
)
;; Price of large chekov short and number of sizes available
(do
  (doseq [i products]
    (if (= "Chekov T-shirt" (:name i)) (do
      (def variants-by-shirt (count (get-in i [:variants])))
      (dotimes [j (count (get-in i [:variants]))] (do
        (def sku (nth (nth (vec (nth (nth (vec i) 3) 1)) j)0))
        (if (= "L" (str (get-in i [:variants (keyword sku) :options :size]))) (do
          (def price-by-shirt (get-in i [:variants (keyword sku) :price :GBP]))
        ))
      ))
    ))
  )
  (println "Price: " price-by-shirt)
  (println "Available quantity" variants-by-shirt)
)

;; Order by id and the names and sizes of each item on the order
(do
  (doseq [i orders]
    (if (= "ab10f1fb-f91b-4bd5-b3ad-21361927b174" (:id i))
      (def order-by-id i)
    )
  )
  (doseq [i (get-in order-by-id [:lines])]
    (def sku (re-find (re-pattern #"[^\s]*") (get-in i [:description])))
    (doseq [j products]
      (if (= (get-in j [:variants (keyword sku) :sku]) sku)
        (println "Name: " (:name j) "Size: "  (get-in j [:variants (keyword sku) :options :size])))
    )
  )
)

;; Delivery order for 16, Stewart's Court, Blackburn, BB6 2HV
(do
  (doseq [i orders]
    (if (= (clojure.string/split "64, Elmington Road, Birmingham, B13 6QG" #",\s*")  (:delivery-address i))
      (def order-for-address i)
    )
  )
  (println order-for-address)
)

;; Calculating turnover
(defn turnover [dir]
  (def turnover-sum 0.0)
  (def files (file-seq (clojure.java.io/file (str "acme-data\\" dir))))
  (defn only-files
    [file-s]
    (filter #(.isFile %) file-s))
  (def files-vec (vec (only-files files)))
  (def data [])
  (doseq [i files-vec]
    (do
      (def data (concat data (parse-stream (clojure.java.io/reader i) true)))
    )
  )
  (def turnover-sum 0.0)
  (doseq [i data] (do
    (def total (float(get-in i [:total :GBP])))
    (def turnover-sum (+ total turnover-sum))
   ))
  (println "Turnover for" dir "is" (format "%.2f" turnover-sum) "(with deliveries)")
)
(println "Turnover for" dir "is" (format "%.2f" turnover-sum) "(with deliveries)")
;; Calculating profit
(do
  (def dir "purchase-orders\\2017")
  (def order-dir (str (clojure.string/replace dir #"purchase-" "")))
  (turnover order-dir)
  (def files (file-seq (clojure.java.io/file (str "acme-data\\" dir))))
  (defn only-files
    [file-s]
    (filter #(.isFile %) file-s))
  (def files-vec (vec (only-files files)))
  (def data [])
  (doseq [i files-vec]
    (do
      (def data (concat data (parse-stream (clojure.java.io/reader i) true)))
    )
  )
  (def total 0)
  (doseq [i data] (do
    (def sum 0)
    (def lines (get-in i [:lines]))
    (doseq [j lines] (do
        (def quantity (float (get-in j [:quantity])))
        (def price (float (get-in j [:price :GBP])))
        (def sum (* quantity price))
    ))
    (def total (+ total sum))
  ))
  (println "Total spent on purchase orders" (format "%.2f" total))
  (def profit (- turnover-sum total))
  (println "Profit" (format "%.2f" profit))
)

;; Top 10 sold products
(do
  (def sold-products [])
  (doseq [i orders] (do
    (def lines (get-in i [:lines]))
    (doseq [j lines] (do
      (def description (get-in j [:description]))
      (def sku (re-find (re-pattern #"[^\s]*") description ))
      (def quantity (first (re-find (re-pattern #"(\d+)(?!.*\d)") description )))
      (doseq [k products] (do
        (if (= (get-in k [:variants (keyword sku) :sku]) sku)
          (repeat (Integer/parseInt quantity)
            (def sold-products (conj sold-products (get-in k [:name])))
          )
        )
      ))
    ))
  ))
  (def sold-products (sort-by val(frequencies sold-products)))
  (def sold-products (take 10 (reverse sold-products)))
  (doseq [i sold-products]
    (println (first i))
    (spit "index.html" (html [:div#top.product (str (first i))]) :append true)
  )
  (spit "index.html" (html [:div#top.product "---------------------"]) :append true)
)

(do
  (def unfulfilled (- (count (map :id orders)) (count (map :id shipments))))
  (println "Number of unfulfilled orders: " unfulfilled)
)

;; Time take to fulfull orders
(do
  (def times [])
  (doseq [i shipments]
    (def built-in-formatter (f/formatters :date-time))
    (def ordered-date  (f/parse built-in-formatter (get-in i [:ordered-at :date])))
    (def shipped-date  (f/parse built-in-formatter (get-in i [:shipped-at :date])))
    (def time-in-hours (t/in-hours (t/interval ordered-date shipped-date)))
    (def times (conj times time-in-hours))
  )
  (def times (sort times))
  (println "Minimum time: " (first times) "Hours")
  (println "Maximum time: " (last times) "Hours")
  (def average (float (/ (reduce + times)  (count times))))
  (println "Average:      " (format "%.2f" average) "Hours Or" (format "%.2f" (float (/ average 24))) "days")
)
