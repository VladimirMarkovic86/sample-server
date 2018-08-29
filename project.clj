(defproject org.vladimir/sample-server "0.1.0"
  :description "Sample server"
  :url "http://gitlab:1610/VladimirMarkovic86/sample-server"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure	"1.9.0"]
         								[org.vladimir/server-lib "0.1.0"]
         								[org.vladimir/mongo-lib "0.1.0"]
         								[org.vladimir/utils-lib "0.1.0"]
         								[org.vladimir/ajax-lib "0.1.0"]
         								[org.vladimir/session-lib "0.1.0"]
         								[org.vladimir/dao-lib "0.1.0"]
         								[org.vladimir/language-lib "0.1.0"]
         								]
  
  ; AOT - Compailation ahead of time
  :main ^:skip-aot sample-server.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :repl-options {:port 8603})
