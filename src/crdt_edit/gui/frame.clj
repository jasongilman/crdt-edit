(ns crdt-edit.gui.frame
  (:require #_[crdt-edit.gui.LogootSwingDocument]
            [seesaw.core :as sw]
            [seesaw.dev :as sd])
  (:import crdt_edit.gui.LogootSwingDocument))


(defn create
  "Creates an instance of the GUI. Returns a map of the swing document
  and the frame."
  [site logoot-doc outgoing]
  
  (let [text-area (sw/text
                    :text ""
                    :multi-line? true
                    :editable? true)
        document (LogootSwingDocument. site logoot-doc outgoing text-area)]
    (.setDocument text-area document)
    
    {:logoot-swing-doc document
     :frame  (sw/frame :title "CRDT Edit",
                       :content (sw/scrollable text-area)
                       :on-close :dispose)}))

(defn display
  [frame]
  (sw/invoke-later
    (-> frame
        sw/pack!
        sw/show!)))

(defn close
  [frame]
  (sw/invoke-later
    (sw/dispose! frame)))

(comment 
  
  ;; Example of creating two collaborating guis.
  ;; TODO do the same thing with systems after creating systems.
  ;; We'll have two systems running on different ports.
  (do
    (def doc1 (l/create))
    (def doc2 (l/create))
    
    (let [outgoing1 (async/chan 1)
          outgoing2 (async/chan 1)]
      
      ;; The outgoing channel for doc2 is the incoming channel for doc1 and vice versa
      (def f1 (create :a doc1 outgoing1 outgoing2))
      (def f2 (create :b doc2 outgoing2 outgoing1))
      
      (display f1)
      (display f2)
      
      ))
  
  )