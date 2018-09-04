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
  [request-start-line
   request]
  (rt/routing
    request-start-line
    request))

(defn start-server
  "Start server"
  []
  (try
    (srvr/start-server
      routing
      {(rsh/access-control-allow-origin) #{"https://sample:8447"
                                           "http://sample:8449"}
       (rsh/access-control-allow-methods) "GET, POST, DELETE, PUT"}
      1616)
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

