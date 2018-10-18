(ns sample-server.core
  (:gen-class)
  (:require [session-lib.core :as ssn]
            [server-lib.core :as srvr]
            [db-lib.core :as db]
            [common-server.core :as rt]
            [ajax-lib.http.response-header :as rsh]))

(defn routing
  "Custom routing function"
  [request]
  (rt/routing
    request))

(defn start-server
  "Start server"
  []
  (try
    (srvr/start-server
      routing
      {(rsh/access-control-allow-origin) #{"https://sample:1613"
                                           "http://sample:1613"
                                           "https://sample-client.herokuapp.com"}
       (rsh/access-control-allow-methods) "OPTIONS, GET, POST, DELETE, PUT"
       (rsh/access-control-allow-credentials) true}
      (or (read-string
            (System/getenv "PORT"))
          1603))
    (db/connect
      "resources/db/")
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

