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
  [logoot-doc text-area document]
  
  (let [;text-area (sw/text
        ;           :text ""
        ;           :multi-line? true
        ;           :editable? true
        ;           ; :listen [:document handle-doc-event]
        ;           )
        ;; TODO pass in channel on which to write changes.
        ; document (RedirectingDocument. nil)
        ]
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
  (def t (sw/text :text ""
                  :multi-line? true
                  :editable? true))
  (def d (RedirectingDocument. nil))
  (def f (create nil t d))
  
  ;; TODO implement the following capabilities 
  ;;  - assume it's only a single character at a time
  ;;  - assume no delete support, only insert
  
  ;; TODO eventually need to add channels to send updates to other instances
  ;; and channels to receive updates from other instances
  
  ;; Idea! We can test this from within one REPLs by creating and displaying two frames
  ;; that are connected by the two channels.
  
  ;; LogootDocument
  ;; extends PlainDocument to add Logoot tracking of changes
  ;; logoot document is in an atom
  
  ;; External changes to the logoot document
  ;;; Changes can come in to the logoot doc via a method that we add on LogootDocument
  ;;; We would first set the write lock, then grab the caret position as a logoot position
  ;;; Then find the logoot position where the text goes and add it. After adding it we
  ;;; would update the caret position to it's new position (covert logoot position to index)
  ;;; Then call bypassInsertString and then unlock the write lock.
  ;;; The gui will be updated and the caret should be in the correct location now.
  
  ;; Typed changes to the logoot document
  ;;; We override the existing insertString function.
  ;;; Use the write lock
  ;;; Find the position within the logoot document to add the new text
  ;;;; (index -> Logoot Position)
  ;;; Insert the text between that logoot position and the one before it
  ;;;; (Logoot Position -> insertable Logoot Position before that position)
  ;;; call bypassInsertString to cause the GUI to update ETC 
  ;;; no caret changes necessary
  ;;; Unlock the write lock
  
  (.bypassInsertString d  0 "X" nil)
  ;; Can position the caret after inserting text since that won't change it.
  (.setCaretPosition t 10)
  
  (display f)
  
  )