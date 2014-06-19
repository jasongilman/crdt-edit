(ns crdt-edit.system
  (:require [crdt-edit.gui.frame :as frame]
            [crdt-edit.logoot :as logoot]
            [crdt-edit.control :as control]
            [clojure.core.async :as async]))

(defn create
  "Creates an initial system"
  [site collaborators]
  (let [outgoing (async/chan 10)
        incoming (async/chan 5)
        logoot-doc (logoot/create)
        {:keys [logoot-swing-doc frame]} (frame/create site logoot-doc outgoing)]
    
    {;; TODO
     :site site
     ;; TODO
     :server nil
     ;; TODO
     :logoot-swing-doc logoot-swing-doc
     ;; TODO
     :frame frame
     ;; TODO
     :logoot-doc logoot-doc
     ;; TODO
     :incoming incoming
     ;; TODO
     :outgoing outgoing
     
     ;; List of host names of collaborating editors
     :collaborators collaborators
     
     ;; Running flag is used to signal go blocks that we are done.
     :running-flag (atom false)}))

(defn start
  "Starts the system and returns it."
  [system]
  (let [{:keys [frame
                running-flag]} system]
    ;; TODO start the server
    
    ;; Set the running flag to true. Must be done before control go blocks start.
    (reset! running-flag true)
    
    ;; Start asynchronously processing incoming changes
    (control/process-incoming system)
    
    ;; TODO send outgoing changes to another server
    ; (control/process-outgoing system)
    
    ;; Display the GUI
    (frame/display frame)
    
    system))

(defn stop
  "Stops the system and returns it"
  [system]
  
  (let [{:keys [frame
                running-flag]} system]
    ;; TODO stop the server
    
    ;; Stop processing changes on the channels
    (reset! running-flag false)
    
    ;; Hide the gui
    (frame/close frame)
    
    system))