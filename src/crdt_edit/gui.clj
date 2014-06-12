(ns crdt-edit.gui
  (:require [crdt-edit.logoot :as l]
            [crdt-edit.RedirectingDocument]
            [seesaw.core :as sw]
            [seesaw.dev :as sd])
  (:import javax.swing.text.DocumentFilter
           crdt_edit.RedirectingDocument))

;; TODO we need to use a document filter here. 
;; How do make changes from updates to the logoot from the remote end but at the same time 
;; capture changes from the keyboard and apply them to the logoot document.
;; one idea to test it would be to add core async that writes letters every second to the doc 
;; at a specific position. I could try interacting with the doc and typing while that's happening

;; May come in handy
#_(proxy
  [javax.swing.text.DocumentFilter] []
  (remove 
    [fb offset length]
    )
  (insertString 
    [fb offset string attrs]
    )
  (replace
    [fb offset length text attrs]
    ))

(defn create
  "Creates an instance of the GUI"
  [logoot-doc]
  
  (let [text-area (sw/text
                    :text ""
                    :multi-line? true
                    :editable? true
                    ; :listen [:document handle-doc-event]
                    )
        ;; TODO pass in channel on which to write changes.
        document (RedirectingDocument. nil)]
    (.setDocument text-area document)
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
  
  (def f (create nil))
  
  (display f)
  
  )