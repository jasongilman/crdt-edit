(ns user
  (:require [clojure.pprint :refer (pprint pp)]
            [clojure.test :refer (run-all-tests run-tests)]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)])
  (:use [clojure.repl]
        [alex-and-georges.debug-repl]))

(def site :jason)

(def collaborators
  [])

(def system nil)

(defn get-var
  [v]
  (var-get (find-var v)))

(defn start []
  (let [create-fn (get-var 'crdt-edit.system/create)
        start-fn (get-var 'crdt-edit.system/start)
        system (create-fn site collaborators)]
    (alter-var-root #'system
                    (constantly (start-fn system)))))

(defn stop []
  (let [stop-fn (get-var 'crdt-edit.system/stop)]
    (alter-var-root #'system (constantly 
                               (when system 
                                 (stop-fn system))))))


(defn reset
  []
  ;; Due to an unfortunate compilation issue I don't have time to diagnose I have to require this here.
  (require 'crdt-edit.system)
  
  (stop)
  (refresh :after 'user/start))

(println "Custom user.clj loaded.")