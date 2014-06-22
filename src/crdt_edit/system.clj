(ns crdt-edit.system
  (:require [crdt-edit.gui.frame :as frame]
            [crdt-edit.logoot :as logoot]
            [crdt-edit.control :as control]
            [clojure.core.async :as async]
            [crdt-edit.api.routes :as api]
            [crdt-edit.discovery :as discovery]))

(defn create
  "Creates an initial system"
  [site collaborators port]
  (let [outgoing (async/chan 10)
        incoming (async/chan 5)
        logoot-doc (logoot/create)
        collaborators-atom (atom collaborators)
        {:keys [logoot-swing-doc frame]} (frame/create site port logoot-doc outgoing collaborators-atom)]
    
    {;; The web server that accepts updates.
     :port port
     :server (api/create-server port)
     
     ;; The java.swing.text.Document that handles typing updates
     :logoot-swing-doc logoot-swing-doc
     
     ;; The swing frame containing the GUI displayed to the user.
     :frame frame
     
     ;; Core.async channel of incoming changes from other collaborators.
     :incoming incoming
     ;; Core.async channel of changes to send to other collaborators.
     :outgoing outgoing
     
     ;; List of host names of collaborating editors
     :collaborators collaborators-atom
     
     ;; Allows discovery of other running editors
     :discovery (discovery/create)
     
     ;; Running flag is used to signal go blocks that we are done.
     :running-flag (atom false)}))

(defn get-logoot-doc
  [system]
  (-> system
      :logoot-swing-doc
      (.data)
      deref
      :logoot-doc))

(defn print-logoot-doc
  [system]
  (println (logoot/logoot-string (get-logoot-doc system))))

(defn start
  "Starts the system and returns it."
  [system]
  (let [{:keys [frame
                running-flag]} system]
    ;; Set the running flag to true. Must be done before control go blocks start.
    (reset! running-flag true)
    
    ;; Start asynchronously processing incoming changes
    (control/process-incoming system)
    
    ;; Send outgoing changes to another server
    (control/process-outgoing system)
    
    ;; Display the GUI
    (frame/display frame)
    
    (-> system
        (update-in [:server] api/start-server system)
        (update-in [:discovery] discovery/start system))))

(defn stop
  "Stops the system and returns it"
  [system]
  
  (let [{:keys [frame
                running-flag]} system]
    ;; Stop processing changes on the channels
    (reset! running-flag false)
    
    ;; Hide the gui
    (frame/close frame)
    
    (-> system
        (update-in [:server] api/stop-server system)
        (update-in [:discovery] discovery/stop system))))


