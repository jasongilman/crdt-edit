(ns crdt-edit.discovery
  "Allows discovery of other editors. Uses the JmDNS library for service registry and discovery."
  (:require [clojure.set :as set]
            [clj-http.client :as client])
  (:import [javax.jmdns 
            JmDNS
            ServiceInfo
            ServiceListener]))

(def service-type "_crdt-edit._tcp.local.")

(defn- service-event->ip-and-port
  "TODO"
  [event]
  [(.getHostAddress (.getInet4Address (.getInfo event)))
   (.getPort (.getInfo event))])

(defn- create-service-info
  "TODO"
  [system]
  (ServiceInfo/create service-type
                      "Collaborative Editor"
                      (:port system)
                      "Collaboratively edit"))

(defn- handle-service-event
  "TODO"
  [event system]
  (try 
    (let [{:keys [collaborators ip-address port]} system
          [event-ip event-port] (service-event->ip-and-port event)]
      (if (and (= event-ip ip-address)
               (= event-port port))
        (println "Ignoring service running on" event-ip event-port)
        (swap! collaborators conj (str event-ip ":" event-port))))
    (catch Exception e
      (.printStackTrace e))))

(defn- create-service-listener
  "TODO"
  [system]
  (reify ServiceListener
    (serviceAdded 
      [this event]
      (println "Service added" event)
      )
    (serviceRemoved
      [this event]
      (println "Service removed")
      )
    (serviceResolved
      [this event]
      (println "Service resolved" event)
      (handle-service-event event system))))

(defn create
  "Creates discovery data"
  []
  {:jmdns (JmDNS/create)
   :service-listener nil
   :registered-service nil})

(defn start
  "Starts listening for other collaborators on the network. When they are
  found they will be added to the list of collaborators."
  [discovery-info system]
  
  ;; Add a watch on the collaborators Atom. When it's updated we should tell the other
  ;; collaborator manually
  
  (add-watch (:collaborators system) :discovery-collaborators 
             (fn [_ _ prev-collaborators new-collaborators]
               (let [changed-collabs (set/difference new-collaborators prev-collaborators)
                     me (str (:ip-address system) ":" (:port system))]
                 (doseq [collaborator changed-collabs]
                   (when (not= me collaborator)
                     (println "Adding" me "as a collaborator to" collaborator)
                     (let [url (format "http://%s/collaborators" collaborator)]
                       (client/post url {:headers {:content-type "application/edn"}
                                         :body (pr-str me)})))))))
  
  (let [jmdns (:jmdns discovery-info)
        service-listener (create-service-listener system)
        registered-service (create-service-info system)]
    (.addServiceListener jmdns service-type service-listener)
    ;; This takes a little bit of time so run in a background thread.
    (future 
      (.registerService jmdns registered-service)
      (println "Service registered"))
    (assoc discovery-info
           :service-listener service-listener
           :registered-service registered-service)))

(defn stop
  "Removes the listeners and the registered service"
  [discovery-info system]
  (when discovery-info
    (let [{:keys [jmdns service-listener registered-service]} discovery-info]
      (.unregisterService jmdns registered-service)
      (.removeServiceListener jmdns service-type service-listener)
      (dissoc discovery-info :service-listener :registered-service))))

