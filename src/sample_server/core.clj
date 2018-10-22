(ns sample-server.core
  (:gen-class)
  (:require [session-lib.core :as ssn]
            [server-lib.core :as srvr]
            [mongo-lib.core :as mon]
            [sample-server.scripts :as scripts]
            [common-server.core :as rt]
            [ajax-lib.http.response-header :as rsh]))

(def db-uri
     (or (System/getenv "PROD_MONGODB")
         "mongodb://admin:passw0rd@127.0.0.1:27017/admin"))

(def db-name
     "sample-db")

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
      {(rsh/access-control-allow-origin) #{"https://sample:8447"
                                           "https://sample:1613"
                                           "http://sample:1613"
                                           "http://sample:8449"}
       (rsh/access-control-allow-methods) "OPTIONS, GET, POST, DELETE, PUT"
       (rsh/access-control-allow-credentials) true}
      (or (read-string
            (System/getenv "PORT"))
          1603)
      {:keystore-file-path
        "certificate/sample_server.jks"
       :keystore-password
        "ultras12"})
    (mon/mongodb-connect
      db-uri
      db-name)
    (scripts/initialize-db-if-needed)
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

