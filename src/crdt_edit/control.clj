(ns crdt-edit.control
  (:require [clojure.core.async :as a :refer [go go-loop <! >!]]
            [crdt-edit.logoot :as l])
  (:import crdt_edit.LogootSwingDocument))

(defn process-remote-updates
  "Takes logoot document changes and applies it to the swing document"
  [incoming ^LogootSwingDocument swing-doc]
  (go
    (while true
      (let [update (<! incoming)
            ;; only works for insert updates
            {:keys [positioned-character]} update]
        (println "Read updated change:" (pr-str positioned-character))
        (.insertPositionedCharacter swing-doc positioned-character)))))