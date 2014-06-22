(ns crdt-edit.control
  (:require [clojure.core.async :as a :refer [go go-loop <! >!]]
            [crdt-edit.logoot :as l]
            [clj-http.client :as client]
            [miner.tagged :as tag])
  (:import crdt_edit.gui.LogootSwingDocument
           crdt_edit.logoot.PositionIdentifier
           crdt_edit.logoot.Position
           crdt_edit.logoot.PositionedCharacter))

(defmulti process-update
  "Processes an update by type"
  (fn [logoot-swing-doc update]
    (:type update)))

(defmethod process-update :insert
  [logoot-swing-doc {:keys [positioned-character]}]
  (.insertPositionedCharacter logoot-swing-doc positioned-character))

(defmethod process-update :remove
  [logoot-swing-doc {:keys [position]}]
  (.removePosition logoot-swing-doc position))

(defn process-incoming
  "Takes incoming logoot document changes and applies it to the swing document"
  [system]
  (let [{:keys [running-flag ^LogootSwingDocument logoot-swing-doc incoming]} system]
    (go
      (while @running-flag
        (try 
          (let [updates (<! incoming)]
            (doseq [update updates]
              (process-update logoot-swing-doc update)))
          (catch Exception e
            (.printStackTrace e)))))))

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
        (let [updates (<! outgoing)
              edn (pr-str updates)]
          (doseq [collaborator collaborators]
            (let [url (format "http://%s/updates" collaborator)]
              (client/post url
                {:headers {:content-type "application/edn"}
                 :body edn}))))))))