(ns crdt-edit.control
  (:require [clojure.core.async :as a :refer [go <! >!]]
            [crdt-edit.logoot :as l]))

(defn process-typed-changes
  "Takes changes from the change channel applies them to the logoot document
  and sends updates to apply to the swing text area on the update-channel."
  [change-channel logoot-doc-atom update-channel]
  (go 
    (while true
      (let [change (<! change-channel)
            ;; only works for insert changes
            {:keys [offset string]} change
            
            ;; TODO update this to find the position where the text will be inserted
            ;; in the logoot document. Will need to iterate over characters and insert each.
            
            position (l/position-at-index @logoot-doc-atom offset)
            pos-char (l/->PositionedCharacter )]
        ; (swap! logoot-doc-atom l/insert )
        ; (>! update-channel {:position position :string})
        
        ))))

(defn process-updates
  "Takes logoot document changes and applies it to the swing document"
  [update-channel swing-doc]
  (go 
    (while true
      (let [update (<! update-channel)
            ;; only works for insert updates
            {:keys [position string]} update]
        ; (>! update-channel {:position position string})
        )))
  
  )