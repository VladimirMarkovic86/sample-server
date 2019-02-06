(ns sample-server.scripts
  (:require [mongo-lib.core :as mon]
            [common-middle.collection-names :refer [db-updates-cname]]
            [common-server.scripts :as css]
            [sample-server.scripts.language :as sssl]
            [sample-server.scripts.role :as sssr]
            [sample-server.scripts.user :as sssu]))

(defn initialize-db
  "Initialize database"
  []
  (css/initialize-db)
  (sssl/insert-labels)
  (sssr/insert-roles)
  (sssu/update-users)
  (mon/mongodb-insert-one
    db-updates-cname
    {:initialized true
     :date (java.util.Date.)})
 )

(defn initialize-db-if-needed
  "Check if database exists and initialize it if it doesn't"
  []
  (try
    (when-not (mon/mongodb-exists
                db-updates-cname
                {:initialized true})
      (initialize-db))
    (catch Exception e
      (println e))
   ))

