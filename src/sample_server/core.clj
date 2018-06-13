(ns sample-server.core
 (:require [server-lib.core :as srvr]
           [utils-lib.core :as utils]
           [mongo-lib.core :as mon]
           [ajax-lib.http.entity-header :as eh]
           [ajax-lib.http.response-header :as rsh]
           [ajax-lib.http.mime-type :as mt]
           [ajax-lib.http.status-code :as stc]))

(def db-name "sample-db")

(defn random-uuid
 "Generate uuid"
 []
 (def uuid (.toString (java.util.UUID/randomUUID))
  )
 uuid)

(def users-map
 [{:email "markovic.vladimir86@gmail.com"
   :password "123"}
  {:email "123"
   :password "123"}])

(defn get-pass-for-email
 "Get password for supplied email"
 [itr
  entity-map
  result]
 (if (< itr (count users-map))
  (let [db-user        (nth users-map itr)
        same-email     (= (:email db-user) (:email entity-map))
        same-password  (= (:password db-user) (:password entity-map))]
       (if same-email
           (if same-password
               (swap! result conj {:status   "success"
                                   :email    "success"
                                   :password "success"})
               (swap! result conj {:email "success"}))
           (recur (inc itr) entity-map result))
   )
  @result))

(defn login-authentication
 "Login authentication"
 [entity-body]
 (let [result (get-pass-for-email 0
                                  entity-body 
                                  (atom {:status   "error"
                                         :email    "error"
                                         :password "error"}))]
  (if (= (:status result)
         "success")
      {:status  (stc/ok)
       :headers {(eh/content-type) (mt/text-plain)
                 (rsh/set-cookie)   (str "session=" (random-uuid) "; "
                                          "Expires=Wed, 30 Aug 2019 00:00:00 GMT; "
                                          "Path=/"
                                          ;"Domain=localhost:1612; "
                                          ;"Secure; "
                                          ;"HttpOnly"
                                          )}
       :body    (str result)}
      {:status  (stc/unauthorized)
       :headers {(eh/content-type) (mt/text-plain)}
       :body    (str result)})
  ))

; Expires=Wed, 30 Aug 2019 00:00:00 GMT
; Max-age=5000
; Domain=localhost:1612
; Path=/
; Secure
; HttpOnly
; SameSite=Strict
; SameSite=Lax

(defn am-i-logged-in
 "Check if user is logged in"
 [session-uuid]
 (if (= session-uuid
        uuid)
     {:status  (stc/ok)
      :headers {(eh/content-type) (mt/text-plain)}}
     {:status  (stc/unauthorized)
      :headers {(eh/content-type) (mt/text-plain)}}))

(defn get-cookie-by-name
 "Reurn cookie value by cookie name"
 [cookies
  cookie-name
  cookie-index]
 (if (< cookie-index (count cookies))
  (let [[cname value] (cookies cookie-index)]
   (if (= cookie-name
          cname)
    (:value value)
    (recur cookies cookie-name (inc cookie-index))
    ))
  nil))


(defn get-cookie
 "Read cookie from request"
 [request
  cookie-name]
 (get-cookie-by-name (into [] (:cookies request))
                     cookie-name
                     0))

(defn build-projection
 ""
 [vector-fields
  include]
 (let [projection  (atom {})]
  (doseq [field vector-fields]
   (swap! projection assoc field include))
  @projection))

(defn get-entities
 "Prepare data for table"
 [request-body]
 (if (empty? (:entity-filter request-body))
  (let [current-page     (:current-page request-body)
        rows             (:rows request-body)
        count-entities   (mon/mongodb-count
                          (:entity-type request-body)
                          (:entity-filter request-body))
        number-of-pages  (if (:pagination request-body)
                          (utils/round-up count-entities rows)
                          nil)
        current-page     (if (= current-page number-of-pages)
                          (dec current-page)
                          current-page)
        entity-type    (:entity-type request-body)
        entity-filter  (:entity-filter request-body)
        projection-vector  (:projection request-body)
        projection-include  (:projection-include request-body)
        projection     (build-projection projection-vector
                                         projection-include)
        qsort          (:qsort request-body)
        collation      (:collation request-body)
        final-result   (atom [])
        db-result      (mon/mongodb-find
                        entity-type
                        entity-filter
                        projection
                        qsort
                        rows
                        (* current-page
                           rows)
                        collation)]
   (if (not= -1 current-page)
    (doseq [single-result db-result]
     (let [ekeys  (if projection-include
                   projection-vector
                   (keys single-result))
           entity-as-vector  (atom [])]
      (swap! entity-as-vector conj (:_id single-result))
      (doseq [ekey ekeys]
       (swap! entity-as-vector conj (ekey single-result))
       )
      (swap! final-result conj @entity-as-vector))
     )
    nil)
   {:status  (stc/ok)
    :headers {(eh/content-type) (mt/text-plain)}
    :body    (str {:status  "success"
                   :data       @final-result
                   :pagination {:current-page     current-page
                                :rows             rows
                                :total-row-count  count-entities}
                   })})
  {:status  (stc/bad-request)
   :headers {(eh/content-type) (mt/text-plain)}
   :body    (str {:status  "error"
                  :error-message "404 Bad request"})}))

(defn get-entity
 "Prepare requested entity for response"
 [request-body]
 (let [entity  (mon/mongodb-find-by-id (:entity-type request-body)
                                       (:_id (:entity-filter request-body))
                )
       entity  (assoc entity :_id (str (:_id entity))
                )]
  (if entity
   {:status (stc/ok)
    :headers {(eh/content-type) (mt/text-plain)}
    :body   (str {:status  "success"
                  :data  entity})}
   {:status (stc/not-found)
    :headers {(eh/content-type) (mt/text-plain)}
    :body   (str {:status  "error"
                  :error-message "There is no entity, for given criteria."})}))
 )

(defn update-entity
 "Update entity"
 [request-body]
 (try
  (mon/mongodb-update-by-id (:entity-type request-body)
                            (:_id request-body)
                            (:entity request-body))
  {:status  (stc/ok)
   :headers {(eh/content-type) (mt/text-plain)}
   :body    (str {:status "success"})}
  (catch Exception ex
   (println (.getMessage ex))
   {:status  (stc/internal-server-error)
    :headers {(eh/content-type) (mt/text-plain)}
    :body    (str {:status "error"})}))
 )

(defn insert-entity
 "Insert entity"
 [request-body]
 (try
  (mon/mongodb-insert-one (:entity-type request-body)
                          (:entity request-body))
  {:status  (stc/ok)
   :headers {(eh/content-type) (mt/text-plain)}
   :body    (str {:status "success"})}
  (catch Exception ex
   (println (.getMessage ex))
   {:status  (stc/internal-server-error)
    :headers {(eh/content-type) (mt/text-plain)}
    :body    (str {:status "error"})}))
 )

(defn delete-entity
 "Delete entity"
 [request-body]
 (try
  (mon/mongodb-delete-by-id (:entity-type request-body)
                            (:_id (:entity-filter request-body))
   )
  {:status  (stc/ok)
   :headers {(eh/content-type) (mt/text-plain)}
   :body    (str {:status "success"})}
  (catch Exception ex
   (println (.getMessage ex))
   {:status  (stc/internal-server-error)
    :headers {(eh/content-type) (mt/text-plain)}
    :body    (str {:status "error"})}))
 )

(defn not-found
 "Requested action not found"
 []
 {:status  (stc/not-found)
  :headers {(eh/content-type) (mt/text-plain)}
  :body    (str {:status  "error"
                 :error-message "404 not found"})})

(defn parse-body
 "Read entity-body from request, convert from string to clojure data"
 [request]
 (read-string (:body request))
 )

(defn routing
 "Routing function"
 [request-start-line
  request]
 (println (str "\n" request))
 (case request-start-line
   "POST /login" (login-authentication (parse-body request))
   "POST /am-i-logged-in" (am-i-logged-in (get-cookie request "session"))
   "POST /get-entities" (get-entities (parse-body request))
   "POST /get-entity" (get-entity (parse-body request))
   "POST /update-entity" (update-entity (parse-body request))
   "POST /insert-entity" (insert-entity (parse-body request))
   "DELETE /delete-entity" (delete-entity (parse-body request))
   {:status 404
    :headers {(eh/content-type) (mt/text-plain)}
    :body (str {:status  "success"})}))

(defn start-server
 "Start server"
 []
 (try
   (srvr/start-server
     routing
     {(rsh/access-control-allow-origin) "https://sample:8447"
      (rsh/access-control-allow-methods) "GET, POST, DELETE, PUT"}
     1616)
   (mon/mongodb-connect db-name)
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

