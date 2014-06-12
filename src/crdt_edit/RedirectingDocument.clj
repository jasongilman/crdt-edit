(ns crdt-edit.RedirectingDocument
  (:gen-class :extends javax.swing.text.PlainDocument
              :constructors {[Object] []}
              
              ;; The bypass methods can bypass the capturing done in insertString and remove
              :exposes-methods {insertString bypassInsertString
                                remove bypassRemove}
              :init init
              :state changeChannel))

(import 'crdt_edit.RedirectingDocument)

(defn- -init
  [change-chan]
  [[] change-chan])

(defn -insertString 
  "Overrides insertString on the super class. It won't insert the string but will instead
  place the attempt on a channel to be processed at the document"
  [this offs string attrs]
  (println "Attempting to insert" string "at position" offs))

(defn -remove 
  "Overrides remove on the super class. It won't remove the string but will instead
  place the attempt on a channel to be processed at the document"
  [this offs len]
  (println "Attempting to remove" len "characters at position" offs))

(comment 
  
  (def d (RedirectingDocument. 5))
  (.insertString d 0 "foo" nil)
  (.bypassInsertString d 0 "foo" nil)
  
  (.remove d 3 1)
  (.bypassRemove d 0 1)
  
  (.getText d 0 (.getLength d))

)