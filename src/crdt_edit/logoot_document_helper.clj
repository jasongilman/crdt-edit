(ns crdt-edit.logoot-document-helper
  "Provides implementations of the functions in the LogootDocument class"
  (:require [crdt-edit.logoot :as l]))


(defn insert-string
  "Handles inserting a string on the GUI as typed in by a user."
  [gui-atom offset string]
  
  (swap! 
    gui-atom 
    (fn [{:keys [logoot-doc site] :as gui-atom-val}]
      (let [new-pos (l/new-position-at-index site logoot-doc offset)
            
            ;; TODO assumes string is only a single characters
            pos-char (l/->PositionedCharacter (first string) new-pos)
            logoot-doc (l/insert logoot-doc pos-char)]
        (println "------------------------")
        (clojure.pprint/pprint logoot-doc)
        (assoc gui-atom-val :logoot-doc logoot-doc)))))
