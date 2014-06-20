(ns crdt-edit.control
  (:require [clojure.core.async :as a :refer [go go-loop <! >!]]
            [crdt-edit.logoot :as l]
            [clj-http.client :as client]
            [miner.tagged :as tag])
  (:import crdt_edit.gui.LogootSwingDocument
           crdt_edit.logoot.PositionIdentifier
           crdt_edit.logoot.Position
           crdt_edit.logoot.PositionedCharacter))

(defn process-incoming
  "Takes incoming logoot document changes and applies it to the swing document"
  [system]
  (let [{:keys [running-flag ^LogootSwingDocument logoot-swing-doc incoming]} system]
    (go
      (while @running-flag
        (let [update (<! incoming)
              ;; only works for insert updates
              {:keys [positioned-character]} update]
          (println "Read updated change:" (pr-str positioned-character))
          (.insertPositionedCharacter logoot-swing-doc positioned-character))))))

;; Add methods to records to allow them to be printed to edn

(defmethod print-method crdt_edit.logoot.PositionIdentifier 
  [this w] 
  (tag/pr-tagged-record-on this w))

(defmethod print-method crdt_edit.logoot.Position
  [this w] 
  (tag/pr-tagged-record-on this w))

(defmethod print-method crdt_edit.logoot.PositionedCharacter
  [this w] 
  (tag/pr-tagged-record-on this w))


(defn process-outgoing
  "Takes incoming logoot document changes and applies it to the swing document"
  [system]
  (let [{:keys [running-flag outgoing collaborators]} system]
    (go
      (while @running-flag
        (let [update (<! outgoing)
              ;; only works for insert updates
              edn (pr-str update)]
          (println "Sending outgoing update:" edn)
          (doseq [collaborator collaborators]
            (let [url (format "http://%s/updates" collaborator)]
              (client/post url
                {:headers {:content-type "application/edn"}
                 :body edn}))))))))