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
              :methods [[insertPositionedCharacter [Object] void]
                        [removePosition [Object] void]]
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
  "Inserts a logoot positioned character."
  [this pos-char]
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
  (.writeLockSuper this)
  
  (try 
    ;; Update the logoot document
    (let [helper-fn (var-get 
                      (find-var 
                        'crdt-edit.gui.logoot-swing-document-helper/remove-deleted-characters))]
      (helper-fn (.data this) offs len))
    
    ;; Call bypassRemove to cause the GUI to update ETC 
    (.bypassRemove this offs len)
    
    (finally 
      (.writeUnlockSuper this))))

(defn- -removePosition
  "Removes a character with the logoot position."
  [this position]
  (.writeLockSuper this)
  (try 
    ;; Update the logoot document
    (let [helper-fn (var-get 
                      (find-var 
                        'crdt-edit.gui.logoot-swing-document-helper/remove-position))
          offset (helper-fn (.data this) position)
          ^JTextComponent text-area (:text-area @(.data this))
          initial-caret-position (.getCaretPosition text-area)]
      
      ;; Call bypassRemove to cause the GUI to update ETC 
      (.bypassRemove this offset 1)
      
      ;; Calling remove  on the document doesn't update the caret position so we have do it 
      ;; manually      
      (when (and (> initial-caret-position 0) (<= offset initial-caret-position))
        (.setCaretPosition text-area (dec initial-caret-position))))
    (finally 
      (.writeUnlockSuper this))))

