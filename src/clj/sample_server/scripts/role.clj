(ns sample-server.scripts.role
  (:require [mongo-lib.core :as mon]
            [common-middle.collection-names :refer [role-cname]]
            [common-middle.role-names :refer [test-privileges-rname]]
            [sample-middle.functionalities :as smfns]
            [sample-middle.role-names :refer [person-admin-rname
                                              person-mod-rname
                                              chart-rname]]))

(defn insert-roles
  "Inserts roles"
  []
  (mon/mongodb-insert-many
    role-cname
    [{:role-name person-admin-rname
      :functionalities [smfns/person-create
                        smfns/person-read
                        smfns/person-update
                        smfns/person-delete]}
     {:role-name person-mod-rname
      :functionalities [smfns/person-read
                        smfns/person-update]}
     {:role-name test-privileges-rname
      :functionalities [smfns/test-person-entity]}
     {:role-name chart-rname
      :functionalities [smfns/chart]}
     ]))

