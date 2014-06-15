(ns crdt-edit.LogootSwingDocument
  "TODO document me"
  (:gen-class :extends javax.swing.text.PlainDocument
              :constructors {[Object Object] []}
              
              ;; The bypass methods can bypass the capturing done in insertString and remove
              :exposes-methods {insertString bypassInsertString
                                remove bypassRemove
                                writeLock writeLockSuper
                                writeUnlock writeUnlockSuper}
              :init init
              :state data))

(import 'crdt_edit.LogootSwingDocument)

(defn- -init
  [site logoot-doc]
  ;; Required here to avoid AOT compiling everything.
  (require 'crdt-edit.logoot-document-helper)
  ;; TODO add more here eventually like a channel to send outgoing changes and an incoming channel
  [[] (atom {:site site
             :logoot-doc logoot-doc})])

(defn -insertString 
  "Overrides insertString on the super class."
  [this offs string attrs]
  (println "Attempting to insert" string "at position" offs)
  
  (.writeLockSuper this)
  
  (try 
    ;; Update the logoot document
    (let [helper-fn (var-get (find-var 'crdt-edit.logoot-document-helper/insert-string))]
      (helper-fn (.data this) offs string))
    
    ;; Call bypassInsertString to cause the GUI to update ETC 
    (.bypassInsertString this offs string attrs)
    
    (finally 
      (.writeUnlockSuper this))))

(defn -remove 
  "Overrides remove on the super class."
  [this offs len]
  (println "Attempting to remove" len "characters at position" offs))

(comment 
  
  (def d (LogootDocument. 5))
  (.insertString d 0 "foo" nil)
  (.bypassInsertString d 0 "foo" nil)
  
  (.remove d 3 1)
  (.bypassRemove d 0 1)
  
  (.getText d 0 (.getLength d))
  
  )