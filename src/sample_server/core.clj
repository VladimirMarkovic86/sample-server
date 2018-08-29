(ns sample-server.core
  (:require [session-lib.core :as ssn]
            [server-lib.core :as srvr]
            [utils-lib.core :as utils]
            [mongo-lib.core :as mon]
            [dao-lib.core :as dao]
            [language-lib.core :as lang]
            [ajax-lib.http.entity-header :as eh]
            [ajax-lib.http.response-header :as rsh]
            [ajax-lib.http.mime-type :as mt]
            [ajax-lib.http.status-code :as stc]))

(def db-name
     "sample-db")

(defn not-found
  "Requested action not found"
  []
  {:status (stc/not-found)
   :headers {(eh/content-type) (mt/text-plain)}
   :body (str {:status "error"
               :error-message "404 not found"})})

(defn parse-body
  "Read entity-body from request, convert from string to clojure data"
  [request]
  (read-string
    (:body request))
 )

(defn routing
  "Routing function"
  [request-start-line
   request]
  (println
    (str
      "\n"
      (dissoc
        request
        :body))
   )
  (if (ssn/am-i-logged-in-fn request)
    (let [[cookie-key
           cookie-value] (ssn/refresh-session
                           request)
          response
           (case request-start-line
             "POST /am-i-logged-in" (ssn/am-i-logged-in request)
             "POST /get-entities" (dao/get-entities (parse-body request))
             "POST /get-entity" (dao/get-entity (parse-body request))
             "POST /update-entity" (dao/update-entity (parse-body request))
             "POST /insert-entity" (dao/insert-entity (parse-body request))
             "DELETE /delete-entity" (dao/delete-entity (parse-body request))
             "POST /logout" (ssn/logout request)
             "POST /get-labels" (lang/get-labels request)
             "POST /set-language" (lang/set-language
                                    request
                                    (parse-body request))
             {:status (stc/not-found)
              :headers {(eh/content-type) (mt/text-plain)}
              :body (str {:status  "success"})})]
      (update-in
        response
        [:headers]
        assoc
        cookie-key
        cookie-value))
    (case request-start-line
      "POST /login" (ssn/login-authentication
                      (parse-body
                        request)
                      (:user-agent request))
      "POST /sign-up" (dao/insert-entity (parse-body request))
      "POST /am-i-logged-in" (ssn/am-i-logged-in request)
      "POST /get-labels" (lang/get-labels request)
      {:status (stc/unauthorized)
       :headers {(eh/content-type) (mt/text-plain)}
       :body (str {:status  "success"})})
   ))

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

