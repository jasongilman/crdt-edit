(ns crdt-edit.gui.LogootSwingDocument
  "Coordinates changes between a java Swing Text Document and a Logoot Document. Utilizes
  the locking built into swing to maintain the same text in both."
  (:gen-class :extends javax.swing.text.PlainDocument
              :constructors {[Object Object Object javax.swing.text.JTextComponent] []}
              
              ;; The bypass methods can bypass the capturing done in insertString and remove
              :exposes-methods {insertString bypassInsertString
                                remove bypassRemove
                                writeLock writeLockSuper
                                writeUnlock writeUnlockSuper}
              :methods [[insertPositionedCharacter [Object] void]]
              :init init
              :state data)
  (:import javax.swing.text.JTextComponent))

(import 'crdt_edit.gui.LogootSwingDocument)

(defn- -init
  [site logoot-doc outgoing text-area]
  ;; Required here to avoid AOT compiling everything.
  (require 'crdt-edit.gui.logoot-swing-document-helper)
  [[] (atom {:site site
             :logoot-doc logoot-doc
             :outgoing outgoing
             :text-area text-area})])

(defn- -insertString 
  "Overrides insertString on the super class."
  [this offs string attrs]
  (println "Attempting to insert" string "at position" offs)
  
  (.writeLockSuper this)
  
  (try 
    ;; Update the logoot document
    (let [helper-fn (var-get 
                      (find-var 
                        'crdt-edit.gui.logoot-swing-document-helper/insert-typed-string))]
      (helper-fn (.data this) offs string))
    
    ;; Call bypassInsertString to cause the GUI to update ETC 
    (.bypassInsertString this offs string attrs)
    
    (finally 
      (.writeUnlockSuper this))))

(defn- -insertPositionedCharacter
  [this pos-char]
  (println "Attempting to insert positioned character" (pr-str pos-char))
  (.writeLockSuper this)
  (try 
    ;; Update the logoot document
    (let [helper-fn (var-get 
                      (find-var 
                        'crdt-edit.gui.logoot-swing-document-helper/insert-positioned-character))
          [offset string] (helper-fn (.data this) pos-char)
          ^JTextComponent text-area (:text-area @(.data this))
          initial-caret-position (.getCaretPosition text-area)]
      
      ;; Call bypassInsertString to cause the GUI to update ETC 
      (.bypassInsertString this offset string nil)

      ;; Calling insert string on the document doesn't update the caret position so we have do it 
      ;; manually      
      (when (<= offset initial-caret-position)
        (.setCaretPosition text-area (inc initial-caret-position))))
    (finally 
      (.writeUnlockSuper this))))

(defn- -remove 
  "Overrides remove on the super class."
  [this offs len]
  (println "Attempting to remove" len "characters at position" offs)
  ;; TODO handle removes. 
  ;; Get the position at the index
  ;; Call delete with it.
  
  )

(comment 
  
  (def d (LogootDocument. 5))
  (.insertString d 0 "foo" nil)
  (.bypassInsertString d 0 "foo" nil)
  
  (.remove d 3 1)
  (.bypassRemove d 0 1)
  
  (.getText d 0 (.getLength d))
  
  )