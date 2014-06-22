(ns crdt-edit.discovery
  "Allows discovery of other editors. Uses the JmDNS library for service registry and discovery."
  (:import [javax.jmdns 
            JmDNS
            ServiceInfo
            ServiceListener]
           java.net.InetAddress))

(def service-type "_crdt-edit._tcp.local.")

(defn- service-event->ip-and-port
  "TODO"
  [event]
  [(.getHostAddress (.getInet4Address (.getInfo event)))
   (.getPort (.getInfo event))])

(defn- create-service-info
  "TODO"
  [system]
  (javax.jmdns.ServiceInfo/create service-type
                                  "Collaborative Editor"
                                  (:port system)
                                  "Collaboratively edit"))
(defn- get-current-host
  "TODO"
  []
  (.getHostAddress (InetAddress/getLocalHost)))

(defn- handle-service-event
  "TODO"
  [event collaborators-atom current-port]
  (try 
   (let [[ip port] (service-event->ip-and-port event)]
      (when-not (and (= (get-current-host) ip)
                     (= port current-port))
        (swap! collaborators-atom conj (str ip ":" port))))
   (catch Exception e
     (.printStackTrace e))))

(defn- create-service-listener
  "TODO"
  [system]
  (let [{:keys [collaborators port]} system]
    (reify ServiceListener
      (serviceAdded 
        [this event]
        (println "Service added")
        )
      (serviceRemoved
        [this event]
        (println "Service removed")
        )
      (serviceResolved
        [this event]
        (println "Service resolved" event)
        (handle-service-event event collaborators port)))))

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
  (let [{:keys [jmdns service-listener registered-service]} discovery-info]
    (.unregisterService jmdns registered-service)
    (.removeServiceListener jmdns service-type service-listener)
    (dissoc discovery-info :service-listener :registered-service)))

