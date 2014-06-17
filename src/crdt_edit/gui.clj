(ns crdt-edit.gui
  (:require [crdt-edit.logoot :as l]
            [crdt-edit.LogootSwingDocument]
            [seesaw.core :as sw]
            [seesaw.dev :as sd]
            [clojure.core.async :as async]
            [crdt-edit.control :as control])
  (:import javax.swing.text.DocumentFilter
           crdt_edit.LogootSwingDocument))


(defn create
  "Creates an instance of the GUI"
  [site logoot-doc outgoing incoming]
  
  (let [text-area (sw/text
                    :text ""
                    :multi-line? true
                    :editable? true)
        document (LogootSwingDocument. site logoot-doc outgoing text-area)]
    (.setDocument text-area document)
    
    ;; Start asynchronously processing incoming changes
    (control/process-remote-updates incoming document)
    
    (sw/frame :title "CRDT Edit",
              :content (sw/scrollable text-area)
              :on-close :dispose)))

(defn display
  [frame]
  (sw/invoke-later
    (-> frame
        sw/pack!
        sw/show!)))

(comment 
  
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