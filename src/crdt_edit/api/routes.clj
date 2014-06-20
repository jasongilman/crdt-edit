(ns crdt-edit.api.routes
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.core :refer :all]
            [ring.adapter.jetty :as jetty]
            [miner.tagged :as tag]
            [clojure.core.async :as a :refer [go go-loop <! >!]]))

(defn define-routes
  [system]
  (let [{:keys [incoming]} system]
    (routes
      (POST "/updates" {body :body}
            (let [update (tag/read-string (slurp body))]
              (println "Received" (pr-str update))
              (go (>! incoming update))
              {:status 200 :body ""}))
      (route/not-found "Not Found"))))

(defn make-api
  [system]
  (-> (define-routes system)
      handler/site ))

(defn create-server
  [port]
  {:port port
   :jetty nil})

(defn start-server
  [server system]
  (let [jetty (jetty/run-jetty (make-api system)
                               {:port (:port server)
                                :join? false})]
    (println "Jetty running on port" (:port server))
    (assoc server :jetty jetty)))

(defn stop-server
  [server system]
  (when-let [jetty (:jetty server)]
    (.stop jetty))
  (dissoc server :jetty))