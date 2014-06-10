(ns crdt-edit.logoot
  "An implementation of the Logoot CRDT.")

(defn- lexi-compare
  "Implements lexigraphical comparison between two vectors. Compares each item from the left side
  finding the first comparison that results in not equal. If all are equal then compares their 
  counts. The longer count will be considered greater."
  [v1 v2]
  (or (first (filterv (partial not= 0) (mapv compare v1 v2)))
      ;; nil means that they were equal for the sizes they had. Compare their counts in that case
      (compare (count v1) (count v2))))

(defrecord PositionIdentifier
  [
   ;; Integer identifying a position
   pos
   ;; A symbol identifying the site
   site
   ]
  java.lang.Comparable
  (compareTo 
    [pid1 pid2]
    (let [{pos1 :pos site1 :site} pid1
          {pos2 :pos site2 :site} pid2]
      (cond 
        (< pos1 pos2) -1
        (> pos1 pos2) 1
        :else 
        (compare site1 site2)))))

(defrecord PositionedCharacter
  [
   ;; A single character with a position in the document.
   character
   
   ;; Sequence of idenfiers indicating the position within the document
   identifiers
   ]
  
  java.lang.Comparable
  (compareTo 
    [pc1 pc2]
    (let [ids1 (:identifiers pc1)
          ids2 (:identifiers pc2)]
      (lexi-compare ids1 ids2))))

(defn pos-id
  "Creates a new position idenfier"
  [pos site]
  (->PositionIdentifier pos site))

(comment 
  
  (compare [1 2 3] [55])
  
  
  (lexi-compare [1 2 3] [1 2]) ; = 1 meaning 123 comes after 12
  (lexi-compare [1 2] [1 2 3]) ; = -1 meaning 1 2 comes before 1 2 3
  
  ;; Normal comparisons of different lengths work
  (lexi-compare [1 2 3] [1 5]) 
  (compare "123" "15") 
  (compare "abc" "ad")
  
  (lexi-compare [1 5] [1 2 3])
  (compare "15" "123")
  
  
  (defn test-compare
    [p1 p2]
    (let [result (compare p1 p2)]
      (case result
        -1 "p1 is less than p2"
        0 "p1 is equal to p2"
        1 "p1 is greater than p2")))
  
  
  (test-compare 
    (->PositionedCharacter 
      \a
      [(pos-id 0 :a)
       (pos-id 1 :a)
       (pos-id 2 :b)
       ; (pos-id 3 :a)
       ])
    (->PositionedCharacter 
      \a
      [(pos-id 0 :a)
       (pos-id 1 :a)
       (pos-id 2 :a)
       ; (pos-id 3 :a)
       ]))
  
  (compare (pos-id 0 :a) (pos-id 0 :a))
  (compare (pos-id 0 :a) (pos-id 0 :b))
  (compare (pos-id 0 :b) (pos-id 0 :a))
  (compare (pos-id 0 :b) (pos-id 1 :b))
  (compare (pos-id 1 :b) (pos-id 0 :b))
  
  
  );; comment end


(defn create
  "Returns a new empty data structure"
  []
  )

(defn insert
  [])

(def delete
  [])