(ns sample-server.scripts
  (:require [mongo-lib.core :as mon]
            [utils-lib.core :as utils]
            [common-middle.collection-names :refer [db-updates-cname
                                                    language-cname
                                                    role-cname
                                                    user-cname
                                                    preferences-cname]]
            [common-middle.role-names :refer [user-admin-rname
                                              user-mod-rname
                                              language-admin-rname
                                              language-mod-rname
                                              role-admin-rname
                                              role-mod-rname
                                              test-privileges-rname]]
            [sample-middle.role-names :refer [person-admin-rname
                                              person-mod-rname]]
            [common-middle.functionalities :as fns]
            [sample-middle.functionalities :as smfns]))

(defn initialize-db
  "Initialize database"
  []
  (mon/mongodb-insert-many
    language-cname
    [{:code 1 :english "Save" :serbian "Сачувај" }
     {:code 2, :english "Log out", :serbian "Одјави се" }
     {:code 3, :english "Home", :serbian "Почетна" }
     {:code 4, :english "Create", :serbian "Креирај" }
     {:code 5, :english "Show all", :serbian "Прикажи све" }
     {:code 6, :english "Details", :serbian "Детаљи" }
     {:code 7, :english "Edit", :serbian "Измени" }
     {:code 8, :english "Delete", :serbian "Обриши" }
     {:code 9, :english "Actions", :serbian "Акције" }
     {:code 10, :english "Insert", :serbian "Упиши" }
     {:code 11, :english "Update", :serbian "Ажурирај" }
     {:code 12, :english "Cancel", :serbian "Откажи" }
     {:code 13, :english "Search", :serbian "Претрага" }
     {:code 14, :english "E-mail", :serbian "Е-пошта" }
     {:code 15, :english "Password", :serbian "Лозинка" }
     {:code 16, :english "Remember me", :serbian "Упамти ме" }
     {:code 17, :english "Log in", :serbian "Пријави се" }
     {:code 18, :english "Sign up", :serbian "Направи налог" }
     {:code 19, :english "Username", :serbian "Корисничко име" }
     {:code 20, :english "Confirm password", :serbian "Потврди лозинку" }
     {:code 21, :english "User", :serbian "Корисник" }
     {:code 23, :english "Language", :serbian "Језик" }
     {:code 24, :english "Label code", :serbian "Код лабеле" }
     {:code 25, :english "English", :serbian "Енглески" }
     {:code 26, :english "Serbian", :serbian "Српски" }
     {:code 27, :english "Functionality", :serbian "Функционалност" }
     {:code 22, :english "Role", :serbian "Улога" }
     {:code 28, :english "Role name", :serbian "Назив улоге" }
     {:code 29, :english "Functionalities", :serbian "Функционалности" }
     {:code 30, :english "Roles", :serbian "Улоге" }
     {:code 1001, :english "Person", :serbian "Особа" }
     {:code 1002, :english "First name", :serbian "Име" }
     {:code 1003, :english "Last name", :serbian "Презиме" }
     {:code 1004, :english "Height", :serbian "Висина" }
     {:code 1005, :english "Weight", :serbian "Тежина" }
     {:code 1006, :english "Birthday", :serbian "Датум рођења" }
     {:code 1007, :english "Gender", :serbian "Пол" }
     {:code 1008, :english "Diet", :serbian "Исхрана" }
     {:code 1009, :english "Activity", :serbian "Активност" }
     {:code 31, :english "No entities", :serbian "Нема ентитета" }])
  (mon/mongodb-insert-many
    role-cname
    [{:role-name user-admin-rname
      :functionalities [fns/user-create
                        fns/user-read
                        fns/user-update
                        fns/user-delete]}
     {:role-name user-mod-rname
      :functionalities [fns/user-read
                        fns/user-update]}
     {:role-name language-admin-rname
      :functionalities [fns/language-create
                        fns/language-read
                        fns/language-update
                        fns/language-delete]}
     {:role-name language-mod-rname
      :functionalities [fns/language-read
                        fns/language-update]}
     {:role-name role-admin-rname
      :functionalities [fns/role-create
                        fns/role-read
                        fns/role-update
                        fns/role-delete]}
     {:role-name role-mod-rname
      :functionalities [fns/role-read
                        fns/role-update]}
     {:role-name person-admin-rname
      :functionalities [smfns/person-create
                        smfns/person-read
                        smfns/person-update
                        smfns/person-delete]}
     {:role-name person-mod-rname
      :functionalities [smfns/person-read
                        smfns/person-update]}
     {:role-name test-privileges-rname
      :functionalities [smfns/test-person-entity]}])
  (let [user-admin-id (:_id
                        (mon/mongodb-find-one
                          role-cname
                          {:role-name user-admin-rname}))
        language-admin-id (:_id
                            (mon/mongodb-find-one
                              role-cname
                              {:role-name language-admin-rname}))
        role-admin-id (:_id
                        (mon/mongodb-find-one
                          role-cname
                          {:role-name role-admin-rname}))
        person-admin-id (:_id
                          (mon/mongodb-find-one
                            role-cname
                            {:role-name person-admin-rname}))
        test-admin-id (:_id
                        (mon/mongodb-find-one
                          role-cname
                          {:role-name test-privileges-rname}))
        encrypted-password (utils/encrypt-password
                             (or (System/getenv "ADMIN_USER_PASSWORD")
                                 "123"))]
    (mon/mongodb-insert-one
      user-cname
      {:username "admin"
       :email "123@123"
       :password encrypted-password
       :roles [user-admin-id
               person-admin-id
               language-admin-id
               role-admin-id
               test-admin-id]}))
  (let [user-id (:_id
                  (mon/mongodb-find-one
                    user-cname
                    {}))]
    (mon/mongodb-insert-one
      preferences-cname
      {:user-id user-id
       :language "serbian"
       :language-name "Srpski" }))
  (mon/mongodb-insert-one
    db-updates-cname
    {:initialized true
     :date (java.util.Date.)})
 )

(defn db-update-1
  "Database update 1"
  []
  (mon/mongodb-insert-many
    language-cname
    [{:code 1010, :english "Test person entity", :serbian "Тестирај ентитет особа" }])
  (mon/mongodb-insert-one
    db-updates-cname
    {:update 1
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
    (when-not (mon/mongodb-exists
                db-updates-cname
                {:update 1})
      (db-update-1))
    (catch Exception e
      (println e))
   ))

