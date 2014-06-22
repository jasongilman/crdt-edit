(ns user
  (:require [clojure.pprint :refer (pprint pp)]
            [clojure.test :refer (run-all-tests run-tests)]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)])
  (:use [clojure.repl]
        [alex-and-georges.debug-repl]))



(def system-a nil)

(def system-b nil)

(defn get-var
  [v]
  (var-get (find-var v)))

(defn start []
  (let [create-fn (get-var 'crdt-edit.system/create)
        start-fn (get-var 'crdt-edit.system/start)
        system-a (create-fn :a #{} 3000)
        system-b (create-fn :b #{} 3001)]
    (alter-var-root #'system-a
                    (constantly (start-fn system-a)))
    #_(alter-var-root #'system-b
                    (constantly (start-fn system-b)))
    ))

(defn stop []
  (let [stop-fn (get-var 'crdt-edit.system/stop)]
    (alter-var-root #'system-a (constantly 
                                 (when system-a 
                                   (stop-fn system-a))))
    #_(alter-var-root #'system-b (constantly 
                                 (when system-b 
                                   (stop-fn system-b)))
                    )))

(defn print-logoot-doc
  []
  ((get-var 'crdt-edit.system/print-logoot-doc) system-a))

(defn reset
  []
  ;; Due to an unfortunate compilation issue I don't have time to diagnose I have to require this here.
  ;; TODO this is causing other issues if something fails to reload
  (require 'crdt-edit.system)
  
  (stop)
  (refresh :after 'user/start))

(println "Custom user.clj loaded.")