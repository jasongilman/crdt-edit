(ns crdt-edit.system
  (:require [crdt-edit.gui.frame :as frame]
            [crdt-edit.logoot :as logoot]
            [crdt-edit.control :as control]
            [clojure.core.async :as async]
            [crdt-edit.api.routes :as api]
            [crdt-edit.discovery :as discovery]
            [clojure.set :as set]
            [clj-http.client :as client])
  (:import java.net.InetAddress))

(defn create
  "Creates an initial system"
  [site port ip-address]
  (let [outgoing (async/chan 10)
        incoming (async/chan 5)
        logoot-doc (logoot/create)
        ip-address (or ip-address (.getHostAddress (InetAddress/getLocalHost)))
        collaborators-atom (atom #{}
                                 ;; Don't allow self as a collaborator
                                 :validator (fn [current-value]
                                              (nil? (get current-value (str ip-address ":" port)))))
        {:keys [logoot-swing-doc frame]} (frame/create site ip-address port logoot-doc outgoing collaborators-atom)]
    
    {:ip-address ip-address
     :port port
     
     ;; The web server that accepts updates.
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

(defn- notify-collaborotors-when-added
  [system]
  (add-watch 
    (:collaborators system) 
    :discovery-collaborators 
    (fn [_ _ prev-collaborators new-collaborators]
      (println (pr-str prev-collaborators) (pr-str new-collaborators))
      (let [changed-collabs (set/difference new-collaborators prev-collaborators)
            my-location (str (:ip-address system) ":" (:port system))]
        (doseq [collaborator changed-collabs]
          (when (not= my-location collaborator)
            (println "Adding" my-location "as a collaborator to" collaborator)
            (let [url (format "http://%s/collaborators" collaborator)]
              (client/post url {:headers {:content-type "application/edn"}
                                :body (pr-str my-location)}))))))))


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
    
    (notify-collaborotors-when-added system)
    
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


