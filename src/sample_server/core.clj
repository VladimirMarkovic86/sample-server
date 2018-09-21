(ns sample-server.core
  (:require [session-lib.core :as ssn]
            [server-lib.core :as srvr]
            [mongo-lib.core :as mon]
            [common-server.core :as rt]
            [ajax-lib.http.response-header :as rsh]))

(def db-name
     "sample-db")

(defn routing
  ""
  [request]
  (rt/routing
    request))

(defn start-server
  "Start server"
  []
  (try
    (srvr/start-server
      routing
      {(rsh/access-control-allow-origin) #{"https://sample:8447"
                                           "https://sample:1613"
                                           "http://sample:1613"
                                           "http://sample:8449"}
       (rsh/access-control-allow-methods) "OPTIONS, GET, POST, DELETE, PUT"
       (rsh/access-control-allow-credentials) true}
      1603
      {:keystore-file-path
        "/home/vladimir/workspace/certificate/jks/sample_server.jks"
       :keystore-password
        "ultras12"})
    (mon/mongodb-connect
      db-name)
    (ssn/create-indexes)
    (catch Exception e
      (println (.getMessage e))
     ))
 )

(defn stop-server
  "Stop server"
  []
  (try
    (srvr/stop-server)
    (mon/mongodb-disconnect)
    (catch Exception e
      (println (.getMessage e))
     ))
 )

(defn unset-restart-server
  "Stop server, unset server atom to nil
   reload project, start new server instance"
  []
  (stop-server)
  (use 'sample-server.core :reload)
  (start-server))

(defn -main [& args]
  (start-server))

