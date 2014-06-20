(defproject crdt-edit "0.1.0-SNAPSHOT"
  :description "An experimental collaborative text editor built on CRDTs"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [seesaw "1.4.4"]
                 [compojure "1.1.8"]
                 [ring "1.3.0"]
                 [ring/ring-jetty-adapter "1.3.0"]
                 [com.velisco/tagged "0.3.4"]
                 [clj-http "0.9.2"]]
  
  :aot [crdt-edit.gui.LogootSwingDocument]
  
  :profiles 
  {:dev {:source-paths ["dev" "src"]
         :dependencies [[org.clojure/test.check "0.5.8"]
                        [org.clojure/tools.namespace "0.2.4"]
                        [org.clojars.gjahad/debug-repl "0.3.3"]]}})
