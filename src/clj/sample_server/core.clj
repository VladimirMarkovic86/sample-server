(ns sample-server.core
  (:gen-class)
  (:require [session-lib.core :as ssn]
            [server-lib.core :as srvr]
            [mongo-lib.core :as mon]
            [sample-server.config :as config]
            [sample-server.scripts :as scripts]
            [common-server.core :as rt]
            [audit-lib.core :refer [audit]]))

(def logged-in-routing-set
  (atom
    #{}))

(def logged-out-routing-set
  (atom
    #{}))

(defn routing
  "Custom routing function"
  [request]
  (rt/add-new-routes
    @logged-in-routing-set
    @logged-out-routing-set)
  (let [response (rt/routing
                   request)]
    (when @config/audit-action-a
      (audit
        request
        response))
    response))

(defn start-server
  "Start server"
  []
  (try
    (let [port (config/define-port)
          access-control-map (config/build-access-control-map)
          certificates-map (config/build-certificates-map)]
      (config/set-thread-pool-size)
      (config/set-audit)
      (srvr/start-server
        routing
        access-control-map
        port
        certificates-map))
    (mon/mongodb-connect
      config/db-uri
      config/db-name)
    (scripts/initialize-db-if-needed)
    (ssn/create-indexes)
    (config/add-custom-entities-to-entities-map)
    (config/set-report-paths)
    (config/read-sign-up-roles)
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

