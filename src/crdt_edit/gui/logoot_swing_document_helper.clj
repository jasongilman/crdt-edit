(ns crdt-edit.gui.logoot-swing-document-helper
  "Provides implementations of the functions in the LogootDocument class"
  (:require [crdt-edit.logoot :as l]
            [clojure.core.async :as a :refer [go <! >!]]))


(defn insert-typed-string
  "Handles inserting a string on the GUI as typed in by a user."
  [gui-atom offset string]
  
  (when (> (count string) 1)
    (throw (Exception. "TODO implement support for strings longer than 1 character")))
  
  (let [gui-data (swap! 
                   gui-atom 
                   (fn [{:keys [logoot-doc site] :as gui-data}]
                     (let [new-pos (l/new-position-at-index site logoot-doc offset)
                           
                           ;; TODO assumes string is only a single characters
                           pos-char (l/->PositionedCharacter (first string) new-pos)
                           logoot-doc (l/insert logoot-doc pos-char)]
                       (assoc gui-data 
                              :logoot-doc logoot-doc
                              :last-insert pos-char))))
        {:keys [outgoing last-insert]} gui-data]
    
    ;; Write the last inserted character to the outgoing changes
    (go (>! outgoing {:positioned-character last-insert})))
  
  nil)

(defn insert-positioned-character
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

