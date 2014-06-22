(ns crdt-edit.logoot
  "An implementation of the Logoot CRDT. See http://hal.archives-ouvertes.fr/docs/00/34/59/11/PDF/main.pdf"
  (:require [clojure.string :as str]))

(defn- lexi-compare
  "Implements lexigraphical comparison between two vectors. Compares each item from the left side
  finding the first comparison that results in not equal. If all are equal then compares their 
  counts. The longer count will be considered greater."
  [v1 v2]
  (or (first (filterv (partial not= 0) (mapv compare v1 v2)))
      ;; nil means that they were equal for the sizes they had. Compare their counts in that case
      (compare (count v1) (count v2))))

(defprotocol ToLogootString
  "Converts a logoot structure into a string for human readability of the logoot structure as shown
  in the Logoot paper."
  (to-logoot-string
    [i]))

;; Represents portion of a position. Contains a number and the site which created this identifier.
(defrecord PositionIdentifier
  [
   ;; Integer identifying a position
   pos-id
   ;; A symbol identifying the site
   site
   ]
  
  java.lang.Comparable
  
  (compareTo 
    [pid1 pid2]
    (let [{pos-id1 :pos-id site1 :site} pid1
          {pos-id2 :pos-id site2 :site} pid2]
      (cond 
        (< pos-id1 pos-id2) -1
        (> pos-id1 pos-id2) 1
        :else 
        (compare site1 site2))))
  
  ToLogootString
  
  (to-logoot-string
    [pid]
    (str "<" (:pos-id pid) "," (:site pid) ">")))

;; Represents a position in a logoot document. 
(defrecord Position
  [
   ;; A list of position identifiers
   identifiers
   ]
  java.lang.Comparable
  
  (compareTo 
    [pos1 pos2]
    (let [ids1 (:identifiers pos1)
          ids2 (:identifiers pos2)]
      (lexi-compare ids1 ids2)))
  
  ToLogootString
  
  (to-logoot-string
    [pc]
    (str/join "." (map to-logoot-string identifiers))))

;; Represents a single character positioned withing a logoot document.
(defrecord PositionedCharacter
  [
   ;; A single character with a position in the document.
   character
   
   ;; The position of the character within the document
   position
   ]
  
  java.lang.Comparable
  
  (compareTo 
    [pc1 pc2]
    (compare (:position pc1) (:position pc2)))
  
  ToLogootString
  
  (to-logoot-string
    [pc]
    (str "(" 
         (to-logoot-string (:position pc))
         ","
         (:character pc)
         ")")))

(defn pos-id
  "Creates a new position idenfier"
  [pos site]
  (->PositionIdentifier pos site))

(def DOCUMENT_BEGINNING
  "Special positioned character representing the beginning of the document"
  (->PositionedCharacter \B (->Position [(->PositionIdentifier 0 :begin)])))

(def DOCUMENT_END
  "Special positioned character representing the end of the document"
  (->PositionedCharacter \E (->Position [(->PositionIdentifier Integer/MAX_VALUE :begin)])))

(defn create
  "Returns a new empty data structure"
  []
  (sorted-set DOCUMENT_BEGINNING DOCUMENT_END))

(defn doc-string
  "Returns the document as a string"
  [document]
  (apply str (map :character (drop-last (drop 1 document)))))

(defn logoot-string
  "Returns the document as a string as represented in the logoot paper"
  [document]
  (str/join "\n" (map to-logoot-string document)))

(defn random-between
  "Returns a random value between s and e exclusive of their values"
  [s e]
  ;; Using 100 as a typical cap for how far forward we'll jump. This is to avoid a problem
  ;; where many typical inserts in a row will generate numbers close to the Integer/MAX_VALUE.
  (let [e (min (+ s 100) e)]
    (+ s (inc (rand-int (dec (- e s)))))))

(defn- sites-ordered?
  "Returns true if the sites are naturally ordered in the order given."
  [site1 site2 site3]
  (and (< (compare site1 site2) 0)
       (< (compare site2 site3) 0)))

;; TODO a problem not accounted for in the intermediate functions below is if the position is already 
;; at Integer/MAX_VALUE or 1 below it or 

(defn- intermediate-of-id-pair
  "Finds the intermediate id between the two ids if possible. Returns nil if not."
  [site id1 id2 random-mid]
  (let [{^long pos-id1 :pos-id site1 :site} id1
        {^long pos-id2 :pos-id site2 :site} id2]
    
    (cond 
      (= pos-id1 pos-id2)
      (when (sites-ordered? site1 site site2)
        ;; The site naturally fits between the two sites
        (pos-id pos-id1 site))
      
      (> pos-id1 pos-id2)
      ;; pos-id1 is greater than pos-id2 which must mean that the previous ids were separated
      ;; by only 1. 
      (pos-id (random-mid pos-id1 Integer/MAX_VALUE) site)
      
      
      ;; Separated by 1
      (= (- pos-id2 pos-id1) 1)
      (when (< (compare site1 site) 0)
        ;; The site naturally comes after the first ids site
        (pos-id pos-id1 site))
      
      ;; separated by more than one
      :else  
      (pos-id (random-mid pos-id1 pos-id2) site))))


(defn intermediate-position
  "Creates an intermediate position between pos1 and pos2. Site is the site of the current agent. 
  Also allows for the random function to be specified that chooses a value between two other values"
  ([site pos-id1 pos-id2]
   (intermediate-position 
     site pos-id1 pos-id2 random-between))
  ([site pos-id1 pos-id2 random-mid]
   (let [ids1 (:identifiers pos-id1)
         ids2 (:identifiers pos-id2)
         possible-pairs (map vector ids1 ids2)]
     (loop [pairs-left possible-pairs
            intermediate-ids []]
       (if (empty? pairs-left)
         ;; Couldn't find a middle position so we'll append an id 
         (->Position 
           (conj intermediate-ids 
                 (cond 
                   (< (count ids1) (count ids2))
                   (let [pos-to-use (:pos-id (nth ids2 (count ids1)))]
                     ;; When ids2 has more than ids1 we have to find a position before that last ids2
                     (pos-id (random-mid Integer/MIN_VALUE pos-to-use) site))
                   
                   (> (count ids1) (count ids2))
                   (let [pos-to-use (:pos-id (nth ids1 (count ids2)))]
                     ;; When ids1 has more than ids2 we have to find a position after that last ids1
                     (pos-id (random-mid pos-to-use Integer/MAX_VALUE) site))
                   
                   :else ; equal
                   (pos-id (random-mid 0 Integer/MAX_VALUE) site))))
         
         (let [[id1 id2] (first pairs-left)
               intermediate-id (intermediate-of-id-pair site id1 id2 random-mid)]
           (if intermediate-id
             ;; Found an id so return that
             (->Position (conj intermediate-ids intermediate-id))
             
             ;; Keep searching
             (recur (rest pairs-left) (conj intermediate-ids id1)))))))))

(defn index->position
  "Returns the position of the document at the given index"
  [document idx]
  (some-> document
          seq
          ;; increment the index since logoot documents have a position representing the beginning 
          ;; of the document
          (nth (inc idx))
          :position))

(defn position->index
  "Returns the index in the document of the given position. Returns nil if not in document."
  [document position]
  (some->> document
       (keep-indexed (fn [index item]
                       (when (= position (:position item))
                         index)))
       first
       ;; Decrement the index since logoot documents have a position representing the beginning
       ;; of the document.
       dec))

(defn new-position-at-index
  "Returns a new position between the characters at the index and 1 before the index."
  [site document idx]
  (let [before-pos (index->position document (dec idx))
        after-pos (index->position document idx)]
    (intermediate-position site before-pos after-pos)))

(defn insert
  "Inserts a positioned character into the logoot document"
  [document pc]
  (conj document pc))

(defn delete
  "Removes a character from the document at the given position."
  [document position]
  (disj document (->PositionedCharacter nil position)))



