(ns crdt-edit.test.logoot
  (:require [clojure.test :refer :all]
            [clojure.test.check.properties :refer [for-all]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [crdt-edit.logoot :as l]
            [clojure.pprint :refer [pprint]]))

(def site-gen
  "Site generator."
  (gen/elements [:a :b :c :d]))

(def position-identifier-gen
  "Position identifier generator."
  (gen/fmap (partial apply l/->PositionIdentifier)
            (gen/tuple gen/pos-int site-gen)))

(def position-gen
  "Position generator"
  (gen/fmap l/->Position
    (gen/vector position-identifier-gen 1 5)))

(def two-unique-positions-in-order
  "Generator of two unique positions in sorted order."
  (gen/fmap 
    sort 
    (gen/such-that 
      (partial apply not=)
      (gen/vector position-gen 2))))

(defn mid
  "Returns the middle integer between two ints" 
  [^long n1 ^long n2]
  (int (/ (+ n1 n2) 2)))

(defspec intermediate-position-spec 1000
  (for-all [positions two-unique-positions-in-order
            site site-gen]
    (let [[pos1 pos2] positions
          ;; TODO only temporarily using mid for now.
          middle-pos (l/intermediate-position site pos1 pos2 mid)
          expected-order [pos1 middle-pos pos2]]
      (and 
        (= expected-order (sort expected-order))
        (not= pos1 middle-pos)
        (not= pos2 middle-pos)))))

(defn pos-builder
  "Helps build positions. Takes a sequence a position int site pairs"
  [& pairs]
  (->> pairs
      (partition 2)
      (map (partial apply l/->PositionIdentifier))
      l/->Position))


(def max-int Integer/MAX_VALUE)
(def min-int Integer/MIN_VALUE)

(deftest intermediate-position-examples
  (are [site pos1-parts pos2-parts expected-parts]
       (let [pos1 (apply pos-builder pos1-parts)
             pos2 (apply pos-builder pos2-parts)
             expected (apply pos-builder expected-parts)
             actual (l/intermediate-position site pos1 pos2 mid)]
         (if (= expected actual)
           true
           (do
             (println "expected:")
             (pprint expected)
             (println "actual")
             (pprint actual))))
       :a [1 :a 5 :a 8 :a] [3 :a] [2 :a]
       :a [4 :a 1 :a] [4 :a 3 :a] [4 :a 2 :a]
       
       ;; Use site to find position in between matching or separated by one.
       :f [5 :a 1 :e] [5 :a 2 :a] [5 :a 1 :f]
       :d [5 :a 1 :e] [5 :a 2 :a] [5 :a 1 :e (mid 0 max-int) :d]
       :b [5 :a 1 :a] [5 :a 1 :c] [5 :a 1 :b]
       
       :r [5 :a 1 :a 10 :a] [5 :a 1 :c] [5 :a 1 :a (mid 10 max-int) :r]
       
       :a [0 :a] [0 :a 0 :a] [0 :a (mid min-int 0) :a]
       :a [3 :a 1 :a] [4 :a 0 :a] [3 :a (mid 1 max-int) :a]
       
       ;; Best case but harder to implement
       ; :a [3 :a 0 :a] [4 :a 0 :a] [3 :a (mid 0 max-int) :a]
       :a [3 :a 0 :a] [4 :a 0 :a] [3 :a 0 :a (mid 0 max-int) :a]
       
       ))


