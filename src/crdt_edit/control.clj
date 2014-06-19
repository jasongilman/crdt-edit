(ns crdt-edit.control
  (:require [clojure.core.async :as a :refer [go go-loop <! >!]]
            [crdt-edit.logoot :as l])
  (:import crdt_edit.gui.LogootSwingDocument))

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