;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
              ;old
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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

;; Data of a shirt
(defn shirttype-by-name [name]
  (doseq [i mini-products]
    (if (= (:name i) name) (println (i :variants)))
  )
)
(shirttype-by-name "Dr. McCoy T-shirt")


;;Price of SKU-1038
(defn price-by-sku [sku]
  (doseq [i mini-products]
    (if (= (get-in i [:variants (keyword sku) :sku]) sku)
      (println (get-in i [:variants (keyword sku) :price :GBP])))
  )
)
(price-by-sku "SKU-1038")

;; Price of large chekov short and number of sizes available
(defn price-by-shirt [name size]
  (doseq [i mini-products]
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
  (doseq [i mini-orders]
    (if (= id (:id i))
      (do
        ;(println i)
        (def lines (get-in i [:lines]))
        (doseq [j lines]
          (def sku (re-find (re-pattern #"[^\s]*") (get-in j [:description])))
          (doseq [k mini-products]
            (if (= (get-in k [:variants (keyword sku) :sku]) sku)
              (println "Name: " (:name k) "\nSize "  (get-in k [:variants (keyword sku) :options :size])))
          )
        )
      )
    )
  )
)
(order-by-id "05d8d404-b3f6-46d1-a0f9-dbdab7e0261f")

;; Delivery order for 16, Stewart's Court, Blackburn, BB6 2HV
(defn order-for-address [address]
  (doseq [i mini-orders]
    (if (= (clojure.string/split address #",\s*")  (:delivery-address i))
      (println i)
    )
  )
)
(order-for-address "16, Stewart's Court, Blackburn, BB6 2HV")

(defn turnover [ordersIn productsIn]
  ;; Calculating turnover
  (def totalsum 0.0)
  (doseq [i ordersIn]
    (do
      (def sum 0)
      (def lines (get-in i [:lines]))
      (doseq [j lines]
        (do
          (def description (get-in j [:description]))
          (def sku (re-find (re-pattern #"[^\s]*") description ))
          (if (= (str "Delivery") description)
            (do
              (def delivery (get-in j [:price :GBP]) )
              ;(println  "Delivery cost: "delivery)
            )
            (do
              (doseq [k productsIn]
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
        )
      )
      (def totalsum (+ sum totalsum))
      ;(println "Total: "(format "%.2f" sum))
    )
  )
  (println "Total turnover (excluding delivery price):"(format "%.2f" totalsum))
)
(turnover mini-orders mini-products)
