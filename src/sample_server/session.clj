(ns sample-server.session
  (:require [mongo-lib.core :as mon]
            [ajax-lib.http.entity-header :as eh]
            [ajax-lib.http.response-header :as rsh]
            [ajax-lib.http.mime-type :as mt]
            [ajax-lib.http.status-code :as stc]))

(def session-timeout-num 30)

(def long-session-timeout-num (* 3 365 24 60 60))

(defn session-timeout
  ""
  [minutes]
  (let [simple-date-format (java.text.SimpleDateFormat. "EE, dd MMM yyyy HH:mm:ss zzz")
        current-date-time (java.util.Date.)
        calendar (java.util.Calendar/getInstance)]
    (.setTime
      calendar
      current-date-time)
    (.add
      calendar
      java.util.Calendar/MINUTE
      minutes)
    (.format
      simple-date-format
      (.getTime
        calendar))
   ))

(defn session-cookie-string
  ""
  [cookie-name
   session-uuid
   timeout-in-minutes]
  (str cookie-name
       "=" session-uuid "; "
       "Expires="
       (session-timeout
         timeout-in-minutes)
       "; "
       "Max-Age=" (* timeout-in-minutes
                     60)
       "; "
       " Path=/"
       ;"Domain=localhost:1612; "
       ;"Secure; "
       ;"HttpOnly"
       ))

; Expires=Wed, 30 Aug 2019 00:00:00 GMT
; Max-age=5000
; Domain=localhost:1612
; Path=/
; Secure
; HttpOnly
; SameSite=Strict
; SameSite=Lax

(defn get-cookie
  "Read cookie from request"
  [cookies
   cookie-name]
  (if-let [cookies cookies]
    (let [cookies (clojure.string/split cookies #"; ")
          cookies-map (atom {})]
      (doseq [cookie cookies]
        (let [[c-name
               c-value] (clojure.string/split cookie #"=")]
          (swap!
            cookies-map
            assoc
            (keyword
              c-name)
            c-value))
       )
     (cookie-name
       @cookies-map))
   ))

(defn am-i-logged-in
 "Check if user is logged in"
 [request]
 (let [cookies (:cookie request)
       session-uuid (or (get-cookie
                          cookies
                          :session)
                        (get-cookie
                          cookies
                          :long-session)
                        -1)]
   (if-let [uuid (mon/mongodb-find-one
                   "user"
                   {:uuid session-uuid})]
     {:status  (stc/ok)
      :headers {(eh/content-type) (mt/text-plain)}
      :body "It's ok"}
     {:status  (stc/unauthorized)
      :headers {(eh/content-type) (mt/text-plain)}
      :body "It's not ok"}))
 )

(defn am-i-logged-in-fn
  ""
  [request]
  (= (:status
       (am-i-logged-in
         request))
     (stc/ok))
 )

(defn refresh-session
  ""
  [request
   response]
  (let [session-uuid (get-cookie
                       (:cookie request)
                       :session)
        long-session-uuid (get-cookie
                            (:cookie request)
                            :long-session)
        [session-uuid
         cookie-name] (if session-uuid
                        [session-uuid
                         "session"]
                        (when long-session-uuid
                          [long-session-uuid
                           "long-session"]))]
    (update-in
      response
      [:headers]
      assoc
      (rsh/set-cookie)
      (session-cookie-string
        cookie-name
        session-uuid
        session-timeout-num))
    response))

(defn session-cookie-string-fn
  ""
  [remember-me
   uuid]
  (if remember-me
    (session-cookie-string
      "long-session"
      uuid
      long-session-timeout-num)
    (session-cookie-string
      "session"
      uuid
      session-timeout-num))
  )

