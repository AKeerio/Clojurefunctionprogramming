(ns tasktwo.core
  (:gen-class)
  (:require clojure.test)
  (:require [cheshire.core :refer :all])
  (require [clj-time.core :as t])
  (require [clj-time.format :as f])
  (require [clj-time.coerce :as c])
)
;-------------------------------------------------------------------------------------------------------
;                                             Full data
;-------------------------------------------------------------------------------------------------------
(def mini-products (parse-stream (clojure.java.io/reader "mini-data\\products.json") true))
(def mini-orders (parse-stream (clojure.java.io/reader "mini-data\\orders.json") true))
(def mini-offers (parse-stream (clojure.java.io/reader "mini-data\\offers.json") true))
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

(println "There are" (count  (map :id products)) "unique products (full data)")
(println "There are" (count  (map :id purchase-orders)) "unique purchase-orders (full data)")
(println "There are" (count  (map :id orders)) "unique orders (full data)")
(println "There are" (count  (map :id shipments)) "unique shipments (full data)")

;; Data of a shirt
(defn shirttype-by-name [name]
  (doseq [i products]
    (if (= (:name i) name) (println (i :variants)))
  )
)
(shirttype-by-name "Dr. McCoy T-shirt")


;;Price of SKU-1038
(defn price-by-sku [sku]
  (doseq [i products]
    (if (= (get-in i [:variants (keyword sku) :sku]) sku)
      (println (get-in i [:variants (keyword sku) :price :GBP])))
  )
)
(price-by-sku "SKU-1038")

;; Price of large chekov short and number of sizes available
(defn price-by-shirt [name size]
  (doseq [i products]
    (if (= (:name i) name)
      (dotimes [j (count (vec (nth (nth (vec i) 3) 1)))]
        (if(= size (str (get-in i [:variants (keyword (nth (nth (vec (nth (nth (vec i) 3) 1)) j)0)) :options :size])))
          (println "There are " (count (vec (nth (nth (vec i) 3) 1))) " available sizes of Chekov shirt."
            "\nPrice of" size name "is "
            (get-in i [:variants
              (keyword (nth (nth (vec (nth (nth (vec i) 3) 1)) j)0))
              :price :GBP])
          )
        )
      )
    )
  )
)
(price-by-shirt "Chekov T-shirt" "L")

;; Order by id and the names and sizes of each item on the order
(defn order-by-id [id]
  (doseq [i orders]
    (if (= id (:id i)) (do
        ;(println i)
        (def lines (get-in i [:lines]))
        (doseq [j lines]
          (def sku (re-find (re-pattern #"[^\s]*") (get-in j [:description])))
          (doseq [k products]
            (if (= (get-in k [:variants (keyword sku) :sku]) sku)
              (println "Name: " (:name k) "\nSize "  (get-in k [:variants (keyword sku) :options :size])))
          )
        )
    ))
  )
)
(order-by-id "ab10f1fb-f91b-4bd5-b3ad-21361927b174")

;; Delivery order for 16, Stewart's Court, Blackburn, BB6 2HV
(defn order-for-address [address]
  (doseq [i orders]
    (if (= (clojure.string/split address #",\s*")  (:delivery-address i))
      (println i)
    )
  )
)
(order-for-address "64, Elmington Road, Birmingham, B13 6QG")

(defn turnover [orders-data]
;; Calculating turnover
  (def totalsum 0.0)
  (doseq [i orders-data] (do
      (def sum 0)
      (def lines (get-in i [:lines]))
      (doseq [j lines] (do
          (def description (get-in j [:description]))
          (def sku (re-find (re-pattern #"[^\s]*") description ))
          (if (= (str "Delivery") description)
            (do
              (def delivery (get-in j [:price :GBP]) )
              ;(println  "Delivery cost: "delivery)
            )
            (do
              (doseq [k products]
                (if (= (get-in k [:variants (keyword sku) :sku]) sku)
                  (def price (get-in k [:variants (keyword sku) :price :GBP]))
                )
              )
              (def multiplier (first (re-find (re-pattern #"(\d+)(?!.*\d)") description )))
              (def sum (+ sum (* (Integer/parseInt multiplier) (float price))))
             ;uncomment if you want to inlucde delivery cost to the total turnover
              ;(def sum (+ sum (float delivery)))
            )
          )
      ))
      (def totalsum (+ sum totalsum))
      ;(println "Total: "(format "%.2f" sum))
    ))
    (println "Total turnover (excluding delivery price):"(format "%.2f" totalsum))
)
(turnover orders)

;; Calculating profit
(defn profit [purchase-orders-data]
  (def total 0)
  (doseq [i purchase-orders-data] (do
    (def sum 0)
    (def lines (get-in i [:lines]))
    (doseq [j lines] (do
        (def quantity (float (get-in j [:quantity])))
        (def price (float (get-in j [:price :GBP])))
        (def sum (* quantity price))
    ))
    (def total (+ total sum))
  ))
  (println "Total spent " total)
)
(profit purchase-orders)

;; Top 10 sold products
(defn top-10-products []
  (def sold-products [])
  (doseq [i mini-orders] (do
    (def lines (get-in i [:lines]))
    (doseq [j lines] (do
      (def description (get-in j [:description]))
      (def sku (re-find (re-pattern #"[^\s]*") description ))
      (def quantity (first (re-find (re-pattern #"(\d+)(?!.*\d)") description )))
      (doseq [k mini-products] (do
        (if (= (get-in k [:variants (keyword sku) :sku]) sku)
          (repeat (Integer/parseInt quantity)
            (def sold-products (conj sold-products (get-in k [:name])))
          )
        )
      ))
    ))
  ))
  (def sold-products (sort-by val(frequencies sold-products)))
  (println (take 10 (reverse sold-products)))
)
(top-10-products)


(defn unfulfilled-orders []
  (def unfulfilled (- (count orders) (count shipments)))
  (println "Number of unfulfilled orders: " unfulfilled)
)
(unfulfilled-orders)

;; Time take to fulfull orders
(defn fulfillment-times []
  (def times [])
  (doseq [i shipments]
    (def built-in-formatter (f/formatters :date-time))
    (def ordered-date  (f/parse built-in-formatter (get-in i [:ordered-at :date])))
    (def shipped-date  (f/parse built-in-formatter (get-in i [:shipped-at :date])))
    (def time-in-hours
      (t/in-hours
        (t/interval orders-date shipped-date)
      )
    )
    (def times (conj times time-in-hours))
  )
  (def times (sort times))
  (println "Minimum time: " (first times) "Hours")
  (println "Maximum time: " (last times) "Hours")
  (def average (float (/ (reduce + times)  (count times))))
  (println "Average:      " (format "%.2f" average) "Hours Or" (format "%.2f" (float (/ average 24))) "days")
  ;(print times)
)
(fulfillment-times)
