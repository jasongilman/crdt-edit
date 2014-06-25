(ns crdt-edit.gui.frame
  (:require [crdt-edit.gui.LogootSwingDocument]
            [seesaw.core :as sw]
            [seesaw.dev :as sd]
            [clojure.string :as str])
  (:import crdt_edit.gui.LogootSwingDocument
           java.net.InetAddress))

(defn update-collborators
  [collaborators-text collaborators-atom]
  (println "Setting collaborators to" collaborators-text)
  (let [collaborators (set (str/split collaborators-text #",\s*"))]
    (reset! collaborators-atom collaborators)))

(defn create
  "Creates an instance of the GUI. Returns a map of the swing document
  and the frame."
  [site ip-address port logoot-doc outgoing collaborators-atom]
  
  (let [collaborators-text-area (sw/text :text (str/join ", " @collaborators-atom)
                                         :editable? true)
        text-area (sw/text
                    :text ""
                    :multi-line? true
                    :wrap-lines? true
                    :editable? true)
        document (LogootSwingDocument. site logoot-doc outgoing text-area)]
    (.setDocument text-area document)
    
    ;; Automatically update the collaborators text area when the collaborators change.
    (add-watch collaborators-atom :collaborators-text-update
               (fn [_ _ _ collaborators]
                 (sw/text! collaborators-text-area (str/join ", " collaborators))))
    
    
    {:logoot-swing-doc document
     :frame  (sw/frame :title (format "CRDT Edit %s:%d for site %s" 
                                      ip-address port site)
                       :content (sw/top-bottom-split 
                                  (sw/horizontal-panel
                                    :items ["Collaborators (comma separated)" 
                                            collaborators-text-area
                                            (sw/button :text "Update"
                                                    :listen [:action (fn [& args] (update-collborators (sw/text collaborators-text-area)
                                                                                                       collaborators-atom))])]) 
                                  (sw/scrollable text-area))
                       :minimum-size [440 :by 380]
                       :on-close :exit)}))

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
