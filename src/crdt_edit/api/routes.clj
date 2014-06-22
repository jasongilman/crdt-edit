(ns crdt-edit.api.routes
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.core :refer :all]
            [ring.adapter.jetty :as jetty]
            [miner.tagged :as tag]
            [clojure.core.async :as a :refer [go go-loop <! >!]]))

(defn define-routes
  [system]
  (let [{:keys [incoming collaborators]} system]
    (routes
      (POST "/updates" {body :body}
            (let [updates (tag/read-string (slurp body))]
              (go (>! incoming updates))
              {:status 200 :body ""}))
      (POST "/collaborators" {collaborator :body}
            (println "Adding collaborator via API" collaborator)
            (swap! collaborators conj collaborator))
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