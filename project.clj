(defproject org.clojars.vladimirmarkovic86/sample-server "0.1.0"
  :description "Sample server"
  :url "http://github.com/VladimirMarkovic86/sample-server"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojars.vladimirmarkovic86/server-lib "0.1.0"]
                 [org.clojars.vladimirmarkovic86/ajax-lib "0.1.0"]
                 [org.clojars.vladimirmarkovic86/session-lib "0.1.0"]
                 [org.clojars.vladimirmarkovic86/common-server "0.1.0"]
                 [org.clojars.vladimirmarkovic86/db-lib "0.1.0"]
                 [environ "1.0.0"]
                 ]

  :min-lein-version "2.0.0"

  :uberjar-name "sample-server-standalone.jar"
  :profiles {:production {:env {:production true}}}
  
  :hooks [environ.leiningen.hooks]
  
  :plugins [[environ/environ.lein "0.3.1"]
            ]
  
  :repl-options {:port 8603})

