(defproject org.clojars.vladimirmarkovic86/sample-server "0.2.1"
  :description "Sample server"
  :url "http://github.com/VladimirMarkovic86/sample-server"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojars.vladimirmarkovic86/server-lib "0.3.35"]
                 [org.clojars.vladimirmarkovic86/ajax-lib "0.1.14"]
                 [org.clojars.vladimirmarkovic86/session-lib "0.2.29"]
                 [org.clojars.vladimirmarkovic86/common-server "0.3.46"]
                 [org.clojars.vladimirmarkovic86/sample-middle "0.3.13"]
                 [org.clojars.vladimirmarkovic86/common-middle "0.2.11"]
                 [org.clojars.vladimirmarkovic86/mongo-lib "0.2.13"]
                 [org.clojars.vladimirmarkovic86/utils-lib "0.4.13"]
                 [org.clojars.vladimirmarkovic86/audit-lib "0.1.37"]
                 [org.clojars.vladimirmarkovic86/pdflatex-lib "0.1.9"]
                 ]

  :min-lein-version "2.0.0"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  
  :main ^:skip-aot sample-server.core
  
  :uberjar-name "sample-server-standalone.jar"
  :profiles {:uberjar {:aot :all}}
  :repl-options {:port 8603})

