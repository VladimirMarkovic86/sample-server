(ns sample-server.config
  (:require [server-lib.core :as srvr]
            [ajax-lib.http.response-header :as rsh]
            [sample-server.person.entity :as persone]
            [common-server.core :as rt]
            [utils-lib.core-clj :as utilsclj]
            [pdflatex-lib.core :as tex]))

(def db-uri
     (or (System/getenv "MONGODB_URI")
         (System/getenv "PROD_MONGODB")
         "mongodb://admin:passw0rd@127.0.0.1:27017/admin"))

(def db-name
     "sample-db")

(def project-absolute-path
     "/home/vladimir/workspace/clojure/projects/sample_server")

(defn define-port
  "Defines server's port"
  []
  (let [port (System/getenv "PORT")
        port (if port
               (read-string
                 port)
               1603)]
    port))

(defn build-access-control-map
  "Build access control map"
  []
  (let [access-control-allow-origin #{"https://sample:8447"
                                      "https://sample:1613"
                                      "http://sample:1613"
                                      "https://sample:1603"
                                      "http://sample:1603"
                                      "https://192.168.1.86:1613"
                                      "http://192.168.1.86:1613"
                                      "https://192.168.1.86:1603"
                                      "http://192.168.1.86:1603"
                                      "http://sample:8449"}
        access-control-allow-origin (if (System/getenv "CLIENT_ORIGIN")
                                      (conj
                                        access-control-allow-origin
                                        (System/getenv "CLIENT_ORIGIN"))
                                      access-control-allow-origin)
        access-control-allow-origin (if (System/getenv "SERVER_ORIGIN")
                                      (conj
                                        access-control-allow-origin
                                        (System/getenv "SERVER_ORIGIN"))
                                      access-control-allow-origin)
         
        access-control-map {(rsh/access-control-allow-origin) access-control-allow-origin
                            (rsh/access-control-allow-methods) "OPTIONS, GET, POST, DELETE, PUT"
                            (rsh/access-control-allow-credentials) true
                            (rsh/access-control-allow-headers) "Content-Type"}]
    access-control-map))

(defn build-certificates-map
  "Build certificates map"
  []
  (let [certificates {:keystore-file-path
                       "certificate/sample_server.jks"
                      :keystore-password
                       "ultras12"}
        certificates (when-not (System/getenv "CERTIFICATES")
                       certificates)]
    certificates))

(defn set-thread-pool-size
  "Set thread pool size"
  []
  (let [core-pool-size (System/getenv "CORE_POOL_SIZE")
        maximum-pool-size (System/getenv "MAXIMUM_POOL_SIZE")
        array-blocking-queue-size (System/getenv "ARRAY_BLOCKING_QUEUE_SIZE")]
    (when core-pool-size
      (reset!
        srvr/core-pool-size
        (read-string
          core-pool-size))
     )
    (when maximum-pool-size
      (reset!
        srvr/maximum-pool-size
        (read-string
          maximum-pool-size))
     )
    (when array-blocking-queue-size
      (reset!
        srvr/maximum-pool-size
        (read-string
          array-blocking-queue-size))
     ))
 )

(def audit-action-a
     (atom false))

(defn set-audit
  "Sets audit from AUDIT_ACTIONS environment variable"
  []
  (let [audit-actions (System/getenv "AUDIT_ACTIONS")
        audit-actions (when audit-actions
                        (let [audit-actions (read-string
                                              audit-actions)]
                          (when (instance?
                                  Boolean
                                  audit-actions)
                            audit-actions))
                       )]
    (reset!
      audit-action-a
      audit-actions))
 )

(defn add-custom-entities-to-entities-map
  "Adds custom entities for this project into entities map from common-server"
  []
  (swap!
    rt/entities-map
    assoc
    :person {:reports persone/reports}))

(defn set-report-paths
  "Sets report paths"
  []
  (let [absolute-path (:out (utilsclj/execute-shell-command
                              "pwd"))
        path-prefix (if (= absolute-path
                           project-absolute-path)
                      ""
                      (str
                        project-absolute-path
                        "/"))]
    (reset!
      tex/reports-templates-path
      (or (System/getenv
            "REPORTS_TEMPLATES_PATH")
          (str
            path-prefix
            @tex/reports-templates-path))
     )
    (reset!
      tex/reports-generated-path
      (or (System/getenv
            "REPORTS_GENERATED_PATH")
          (str
            path-prefix
            @tex/reports-generated-path))
     ))
 )

