(ns crdt-edit.gui.frame
  (:require [crdt-edit.gui.LogootSwingDocument]
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
