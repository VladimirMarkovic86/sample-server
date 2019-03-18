(ns sample-server.config
  (:require [server-lib.core :as srvr]
            [ajax-lib.http.response-header :as rsh]))

(def db-uri
     (or (System/getenv "MONGODB_URI")
         (System/getenv "PROD_MONGODB")
         "mongodb://admin:passw0rd@127.0.0.1:27017/admin"))

(def db-name
     "sample-db")

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
  (let [thread-pool-size (System/getenv "THREAD_POOL_SIZE")]
    (when thread-pool-size
      (reset!
        srvr/thread-pool-size
        (read-string
          thread-pool-size))
     ))
 )

