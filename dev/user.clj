(ns user
  (:require [clojure.pprint :refer (pprint pp)]
            [clojure.test :refer (run-all-tests)]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)])
  (:use [clojure.repl]
        [alex-and-georges.debug-repl]))

(defn reset []
  (refresh))

(println "Custom user.clj loaded.")