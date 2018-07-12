(ns sample-server.core
  (:require [session-lib.core :as ssn
                              :refer [am-i-logged-in
                                      am-i-logged-in-fn
                                      session-cookie-string-fn
                                      refresh-session]]
            [server-lib.core :as srvr]
            [utils-lib.core :as utils]
            [mongo-lib.core :as mon]
            [ajax-lib.http.entity-header :as eh]
            [ajax-lib.http.response-header :as rsh]
            [ajax-lib.http.mime-type :as mt]
            [ajax-lib.http.status-code :as stc]))

(def db-name
     "sample-db")

(defn create-indexes
  "Create indexes for system to work"
  []
  (when (not (mon/mongodb-index-exists?
               "user"
               "username-unique-idx"))
    (mon/mongodb-create-index
      "user"
      {:username 1}
      "username-unique-idx"
      true))
  (when (not (mon/mongodb-index-exists?
               "user"
               "email-unique-idx"))
    (mon/mongodb-create-index
      "user"
      {:email 1}
      "email-unique-idx"
      true))
  (when (not (mon/mongodb-index-exists?
               "session"
               "short-session-idx"))
    (mon/mongodb-create-index
      "session"
      {:created-at 1}
      "short-session-idx"
      false
      ssn/session-timeout-num))
  (when (not (mon/mongodb-index-exists?
               "session"
               "session-uuid-unique-idx"))
    (mon/mongodb-create-index
      "session"
      {:uuid 1}
      "session-uuid-unique-idx"
      true))
  (when (not (mon/mongodb-index-exists?
               "long-session"
               "long-session-idx"))
    (mon/mongodb-create-index
      "long-session"
      {:created-at 1}
      "long-session-idx"
      false
      ssn/long-session-timeout-num))
  (when (not (mon/mongodb-index-exists?
               "long-session"
               "long-session-uuid-unique-idx"))
    (mon/mongodb-create-index
      "long-session"
      {:uuid 1}
      "long-session-uuid-unique-idx"
      true))
 )

(defn get-pass-for-email-username
  "Get password for supplied email"
  [email-username
   password]
  (if-let [user-username (mon/mongodb-find-one
                           "user"
                           {:username email-username})]
    (let [db-password (:password user-username)]
      (if (= db-password
             password)
        [{:status   "success"
          :email    "success"
          :password "success"}
         user-username]
        {:status   "error"
         :email    "success"
         :password "error"}))
    (if-let [user-email (mon/mongodb-find-one
                          "user"
                          {:email email-username})]
      (let [db-password (:password user-email)]
        (if (= db-password
               password)
          [{:status   "success"
            :email    "success"
            :password "success"}
           user-email]
          {:status   "error"
           :email    "success"
           :password "error"}))
      {:status   "error"
       :email    "error"
       :password "error"}))
 )

(defn login-authentication
  "Login authentication"
  [request-body
   user-agent]
  (let [email-username (:email request-body)
        password (:password request-body)
        remember-me (:remember-me request-body)
        [result
         user] (get-pass-for-email-username
                 email-username
                 password)]
          
    (if (= (:status result)
           "success")
      (let [uuid (.toString (java.util.UUID/randomUUID))
            session-cookie (session-cookie-string-fn
                             remember-me
                             user
                             uuid
                             user-agent)]
        (if session-cookie
          {:status (stc/ok)
           :headers {(eh/content-type) (mt/text-plain)
                     (rsh/set-cookie) session-cookie}
           :body (str result)}
          {:status (stc/internal-server-error)
           :headers {(eh/content-type) (mt/text-plain)}
           :body (str result)})
       )
      {:status (stc/unauthorized)
       :headers {(eh/content-type) (mt/text-plain)}
       :body (str result)})
   ))

(defn build-projection
  "Build mongo projection map"
  [vector-fields
   include]
  (let [projection (atom {})]
    (doseq [field vector-fields]
      (swap!
        projection
        assoc
        field
        include))
    @projection))

(defn get-entities
  "Prepare data for table"
  [request-body]
  (try
    (let [current-page (:current-page request-body)
          rows (:rows request-body)
          count-entities (mon/mongodb-count
                           (:entity-type request-body)
                           (:entity-filter request-body))
          number-of-pages (when (:pagination request-body)
                            (utils/round-up
                              count-entities
                              rows))
          current-page (if (= current-page
                              number-of-pages)
                         (dec
                           current-page)
                         current-page)
          entity-type (:entity-type request-body)
          entity-filter (:entity-filter request-body)
          projection-vector (:projection request-body)
          projection-include (:projection-include request-body)
          projection (build-projection
                       projection-vector
                       projection-include)
          qsort (:qsort request-body)
          collation (:collation request-body)
          final-result (atom [])
          db-result (mon/mongodb-find
                      entity-type
                      entity-filter
                      projection
                      qsort
                      rows
                      (* current-page
                         rows)
                      collation)]
      (when (not= current-page
                -1)
        (doseq [single-result db-result]
          (let [ekeys (if projection-include
                        projection-vector
                        (keys single-result))
                entity-as-vector (atom [])]
            (swap!
              entity-as-vector
              conj
              (:_id single-result))
            (doseq [ekey ekeys]
              (swap!
                entity-as-vector
                conj
                (ekey single-result))
             )
            (swap!
              final-result
              conj
              @entity-as-vector))
         ))
      {:status (stc/ok)
       :headers {(eh/content-type) (mt/text-plain)}
       :body (str {:status  "success"
                   :data       @final-result
                   :pagination {:current-page     current-page
                                :rows             rows
                                :total-row-count  count-entities}})
       })
    (catch Exception ex
      (println (.getMessage ex))
      {:status (stc/internal-server-error)
       :headers {(eh/content-type) (mt/text-plain)}
       :body (str
               {:status "Error"
                :message (.getMessage ex)})})
   ))

(defn get-entity
  "Prepare requested entity for response"
  [request-body]
  (let [entity (mon/mongodb-find-by-id
                 (:entity-type request-body)
                 (:_id (:entity-filter request-body))
                )
        entity (assoc
                 entity
                 :_id
                 (str
                   (:_id entity))
                )]
    (if entity
      {:status (stc/ok)
       :headers {(eh/content-type) (mt/text-plain)}
       :body   (str
                 {:status  "success"
                  :data  entity})}
      {:status (stc/not-found)
       :headers {(eh/content-type) (mt/text-plain)}
       :body (str
               {:status  "error"
                :error-message "There is no entity, for given criteria."})})
   ))

(defn update-entity
  "Update entity"
  [request-body]
  (try
    (mon/mongodb-update-by-id
      (:entity-type request-body)
      (:_id request-body)
      (:entity request-body))
    {:status (stc/ok)
     :headers {(eh/content-type) (mt/text-plain)}
     :body (str
             {:status "success"})}
    (catch Exception ex
      (println (.getMessage ex))
      {:status (stc/internal-server-error)
       :headers {(eh/content-type) (mt/text-plain)}
       :body (str
               {:status "Error"
                :message (.getMessage ex)})})
   ))

(defn insert-entity
  "Insert entity"
  [request-body]
  (try
    (mon/mongodb-insert-one
      (:entity-type request-body)
      (:entity request-body))
    {:status (stc/ok)
     :headers {(eh/content-type) (mt/text-plain)}
     :body (str
             {:status "success"})}
    (catch Exception ex
      (println (.getMessage ex))
      {:status (stc/internal-server-error)
       :headers {(eh/content-type) (mt/text-plain)}
       :body (str
               {:status "Error"
                :message (.getMessage ex)})})
   ))

(defn delete-entity
  "Delete entity"
  [request-body]
  (try
    (mon/mongodb-delete-by-id
      (:entity-type request-body)
      (:_id
        (:entity-filter request-body))
     )
    {:status (stc/ok)
     :headers {(eh/content-type) (mt/text-plain)}
     :body (str
             {:status "success"})}
    (catch Exception ex
      (println (.getMessage ex))
      {:status (stc/internal-server-error)
       :headers {(eh/content-type) (mt/text-plain)}
       :body (str
               {:status "Error"
                :message (.getMessage ex)})})
   ))

(defn logout
  "Logout user from system"
  [request]
  (ssn/delete-session-record
    request))

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
  (println (str "\n" request))
  (if (am-i-logged-in-fn request)
    (let [[cookie-key
           cookie-value] (refresh-session
                           request)
          response
           (case request-start-line
             "POST /am-i-logged-in" (am-i-logged-in request)
             "POST /get-entities" (get-entities (parse-body request))
             "POST /get-entity" (get-entity (parse-body request))
             "POST /update-entity" (update-entity (parse-body request))
             "POST /insert-entity" (insert-entity (parse-body request))
             "DELETE /delete-entity" (delete-entity (parse-body request))
             "POST /logout" (logout request)
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
      "POST /login" (login-authentication
                      (parse-body
                        request)
                      (:user-agent request))
      "POST /sign-up" (insert-entity (parse-body request))
      "POST /am-i-logged-in" (am-i-logged-in request)
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
      {(rsh/access-control-allow-origin) "https://sample:8447"
       (rsh/access-control-allow-methods) "GET, POST, DELETE, PUT"}
      1616)
    (mon/mongodb-connect
      db-name)
    (create-indexes)
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

