(ns tasktwo.core-test
  (:require [clojure.test :refer :all]
            [tasktwo.core :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1)))
)
(deftest a-test2
  (testing "FIXME, I fail."
    (is (=
        (array-map {:SKU-1011 {:price {:GBP 14.99, :USD 16.99, :EUR 16.99},
          :options {:size S}, :sku SKU-1011}, :SKU-1012 {:price {:GBP 14.99,
          :USD 16.99, :EUR 16.99}, :options {:size M}, :sku SKU-1012}, :SKU-1013
          {:price {:GBP 14.99, :USD 16.99, :EUR 16.99}, :options {:size L},
          :sku SKU-1013}, :SKU-1014 {:price {:GBP 14.99, :USD 16.99, :EUR 16.99},
          :options {:size XL}, :sku SKU-1014}, :SKU-1015 {:price {:GBP 14.99,
          :USD 16.99, :EUR 16.99}, :options {:size XXL}, :sku SKU-1015}})
        ((shirttype-by-name "Dr. McCoy T-shirt"))
    ))
  )
)
