(ns crdt-edit.logoot
  "An implementation of the Logoot CRDT. See http://hal.archives-ouvertes.fr/docs/00/34/59/11/PDF/main.pdf"
  (:require [clojure.string :as str]))

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
    ;; TODO implement comparable for PositionIdentifier
    -1
    )
  
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
    ;; TODO implement comparable for Position
    -1
    )
  
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
    ;; TODO implement comparable for PositionedCharacter
    -1
    )
  
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

(defn intermediate-position
  "Creates an intermediate position between pos1 and pos2. Site is the site of the current agent. 
  Also allows for the random function to be specified that chooses a value between two other values"
  [site pos-id1 pos-id2]
  
  ;; TODO implement this
  
  ;; Note that with example based testing it may be useful to accept a fourth optional argument
  ;; of a function that will find a number between two others. When implementing this we should use
  ;; a random function. Example based tests work better when they can predict the exact output and would
  ;; pass in a function that wwould be more predictive like average.
  
  )

(defn index->position
  "Returns the position of the document at the given index"
  [document idx]
  ;; TODO implement this
  )

(defn position->index
  "Returns the index in the document of the given position. Returns nil if not in document."
  [document position]
  ;; TODO implement this
  )

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



