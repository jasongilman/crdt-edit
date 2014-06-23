(ns crdt-edit.runner
  (:require [crdt-edit.system :as system]
            [clojure.tools.cli :as cli])
  (:import java.net.InetAddress)
  (:gen-class))

(def cli-options
  ;; An option with a required argument
  [["-p" "--port PORT" "Port number"
    :id :port
    :default 3000
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-i" "--ip-address IP" "Current IP Address"
    :id :ip-address]
   ["-s" "--site SITE" "Site"
    :id :site
    :default (keyword (.getHostAddress (InetAddress/getLocalHost)))
    :parse-fn keyword]
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn -main
  [& args]
  (let [{{:keys [port site ip-address]} :options} (cli/parse-opts args cli-options)
        system (system/start (system/create site port ip-address))]
    (println "Running...")))