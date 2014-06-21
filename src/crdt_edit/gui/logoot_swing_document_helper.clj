(ns crdt-edit.gui.logoot-swing-document-helper
  "Provides implementations of the functions in the LogootDocument class"
  (:require [crdt-edit.logoot :as l]
            [clojure.core.async :as a :refer [go <! >!]]))

(defn- insert-string-at-offset
  "Inserts the string into the logoot document at the given offset. 
  Returns the logoot-doc and a list of the positioned characters that were added."
  ([site logoot-doc offset string]
   (insert-string-at-offset site logoot-doc offset string []))
  ([site logoot-doc offset string chars-added]
   (if (empty? string)
     [logoot-doc chars-added]
     (let [new-pos (l/new-position-at-index site logoot-doc offset)
           pos-char (l/->PositionedCharacter (first string) new-pos)]
       (recur site
              (l/insert logoot-doc pos-char) 
              (inc offset) 
              (rest string) 
              (conj chars-added pos-char))))))

(defn handle-insert-string
  "Handles inserting a string on the GUI as typed in by a user."
  [gui-atom offset string]
  
  (let [gui-data (swap! 
                   gui-atom 
                   (fn [{:keys [logoot-doc site] :as gui-data}]
                     (let [[logoot-doc chars-added] 
                           (insert-string-at-offset site logoot-doc offset string)]
                       (assoc gui-data 
                              :logoot-doc logoot-doc
                              :last-inserts chars-added))))
        {:keys [outgoing last-inserts]} gui-data]
    
    ;; Write the last inserted character to the outgoing changes
    (go 
      (doseq [inserted last-inserts]
        (>! outgoing {:type :insert
                      :positioned-character inserted}))))
  nil)

(defn handle-insert-positioned-character
  "Handles inserting a positioned character. Returns a tuple of the offset and the string to insert 
  in the text area."
  [gui-atom pos-char]
  
  (let [gui-data (swap! 
                   gui-atom 
                   (fn [{:keys [logoot-doc] :as gui-data}]
                     (let [logoot-doc (l/insert logoot-doc pos-char)]
                       (assoc gui-data :logoot-doc logoot-doc))))
        updated-doc (:logoot-doc gui-data)]
    [(l/position->index updated-doc (:position pos-char))
     (str (:character pos-char))]))

(defn- remove-at-offset
  "Removes the characters at the document at the given offset and length.
  Returns the updated logoot document and the positions removed"
  ([logoot-doc offset length]
   (remove-at-offset logoot-doc offset length []))
  ([logoot-doc offset length positions-removed]
   (if (= length 0)
     [logoot-doc positions-removed]
     (let [position (l/index->position logoot-doc offset)]
       (recur (l/delete logoot-doc position)
              offset
              (dec length)
              (conj positions-removed position))))))

(defn handle-remove
  "Handles delete characters removed by typing delete/backspace in the text area."
  [gui-atom offset length]
  
  (let [gui-data (swap! 
                   gui-atom 
                   (fn [{:keys [logoot-doc] :as gui-data}]
                     (let [[logoot-doc positions-removed]
                           (remove-at-offset logoot-doc offset length)]
                       (assoc gui-data 
                              :logoot-doc logoot-doc
                              :last-removes positions-removed))))
        {:keys [outgoing last-removes]} gui-data]
    
    ;; Write the last inserted character to the outgoing changes
    (go 
      (doseq [position last-removes]
        (>! outgoing {:type :remove
                      :position position}))))
  nil)

(defn handle-remove-position
  "Handles delete characters received from incoming. Returns the offset of the character that was 
  removed."
  [gui-atom position]
  (let [gui-data (swap! 
                   gui-atom 
                   (fn [{:keys [logoot-doc] :as gui-data}]
                     (let [pos-index (l/position->index logoot-doc position)
                           logoot-doc (l/delete logoot-doc position)]
                       (assoc gui-data 
                              :logoot-doc logoot-doc
                              :last-remove pos-index))))]
    (:last-remove gui-data)))