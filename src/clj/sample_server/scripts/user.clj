(ns sample-server.scripts.user
  (:require [mongo-lib.core :as mon]
            [common-middle.collection-names :refer [user-cname
                                                    role-cname]]
            [common-middle.role-names :refer [test-privileges-rname]]
            [sample-middle.role-names :refer [person-admin-rname
                                              chart-rname]]))

(defn update-users
  "Updates users"
  []
  (let [person-admin-id (:_id
                          (mon/mongodb-find-one
                            role-cname
                            {:role-name person-admin-rname}))
        test-privileges-id (:_id
                             (mon/mongodb-find-one
                               role-cname
                               {:role-name test-privileges-rname}))
        chart-rname-id (:_id
                         (mon/mongodb-find-one
                           role-cname
                           {:role-name chart-rname}))
        sample-admin-roles [person-admin-id
                            test-privileges-id
                            chart-rname-id]
        sample-guest-roles [person-admin-id
                            chart-rname-id]]
    (mon/mongodb-update-one
      user-cname
      {:username "admin"}
      {:$addToSet
        {:roles
          {:$each sample-admin-roles}}
       })
    (mon/mongodb-update-one
      user-cname
      {:username "guest"}
      {:$addToSet
        {:roles
          {:$each sample-guest-roles}}
       }))
 )

