(ns sample-server.scripts
  (:require [mongo-lib.core :as mon]
            [utils-lib.core :as utils]))

(defn initialize-db
  ""
  []
  (mon/mongodb-insert-many
    "language"
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
    "role"
    [{:role-name "User administrator",
      :functionalities [ "user-create", "user-read", "user-update", "user-delete" ] }
     {:role-name "Person administrator",
      :functionalities [ "person-create", "person-read", "person-update", "person-delete" ] }
     {:role-name "Language administrator",
      :functionalities [ "language-create", "language-read", "language-update", "language-delete" ] }
     {:role-name "User moderator",
      :functionalities [ "user-read", "user-update" ] }
     {:role-name "Person moderator",
      :functionalities [ "person-read", "person-update" ] }
     {:role-name "Language moderator",
      :functionalities [ "language-read", "language-update" ] }
     {:role-name "Role administrator",
      :functionalities [ "role-create", "role-read", "role-update", "role-delete" ] }
     {:role-name "Role moderator",
      :functionalities [ "role-read", "role-update" ] }
     {:role-name "Role moderator 2",
      :functionalities [ "role-read" ]}])
  (let [user-admin-id (:_id
                        (mon/mongodb-find-one
                          "role"
                          {:role-name "User administrator"}))
        person-admin-id (:_id
                          (mon/mongodb-find-one
                            "role"
                            {:role-name "Person administrator"}))
        language-admin-id (:_id
                            (mon/mongodb-find-one
                              "role"
                              {:role-name "Language administrator"}))
        role-admin-id (:_id
                        (mon/mongodb-find-one
                          "role"
                          {:role-name "Role administrator"}))
        encrypted-password (utils/encrypt-password
                             (or (System/getenv "ADMIN_USER_PASSWORD")
                                 "123"))]
    (mon/mongodb-insert-one
      "user"
      {:username "admin"
       :email "123@123"
       :password encrypted-password
       :roles [user-admin-id person-admin-id language-admin-id role-admin-id]}))
  (let [user-id (:_id
                  (mon/mongodb-find-one
                    "user"
                    {}))]
    (mon/mongodb-insert-one
      "preferences"
      {:user-id user-id, :language "serbian", :language-name "Srpski" }))
 )

(defn initialize-db-if-needed
  ""
  []
  (try
    (when-not (mon/mongodb-exists
                "language"
                {:english "Save"})
      (initialize-db))
    (catch Exception e
      (println e))
   ))

