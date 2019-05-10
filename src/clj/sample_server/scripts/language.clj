(ns sample-server.scripts.language
  (:require [mongo-lib.core :as mon]
            [common-middle.collection-names :refer [language-cname]]))

(defn insert-labels
  "Inserts labels"
  []
  (mon/mongodb-insert-many
    language-cname
    [{:code 1001, :english "Person", :serbian "Особа" }
     {:code 1002, :english "First name", :serbian "Име" }
     {:code 1003, :english "Last name", :serbian "Презиме" }
     {:code 1004, :english "Height", :serbian "Висина" }
     {:code 1005, :english "Weight", :serbian "Тежина" }
     {:code 1006, :english "Birthday", :serbian "Датум рођења" }
     {:code 1007, :english "Gender", :serbian "Пол" }
     {:code 1008, :english "Diet", :serbian "Исхрана" }
     {:code 1009, :english "Activity", :serbian "Активност" }
     {:code 1010, :english "Test person entity", :serbian "Тестирај ентитет особа" }
     {:code 1011, :english "Mainly sitting", :serbian "Углавном седење" }
     {:code 1012, :english "Easy physical labor", :serbian "Лак физички рад" }
     {:code 1013, :english "Medium physical labor", :serbian "Средње тежак физички рад" }
     {:code 1014, :english "Hard physical labor", :serbian "Тежак физички рад" }
     {:code 1015, :english "Very hard physical labor", :serbian "Веома тежак физички рад" }
     {:code 1016, :english "All", :serbian "Све" }
     {:code 1017, :english "Vegetarian", :serbian "Вегетаријанска" }
     {:code 1018, :english "Male", :serbian "Мушки" }
     {:code 1019, :english "Female", :serbian "Женски" }
     {:code 1020, :english "Chart title", :serbian "Наслов графика" }
     {:code 1021, :english "X axis title", :serbian "Наслов апсцисе" }
     {:code 1022, :english "Y axis title", :serbian "Наслов ординате" }
     {:code 1023, :english "line", :serbian "линија" }
     {:code 1024, :english "data", :serbian "податак" }
     {:code 1025, :english "bar", :serbian "traka" }
     {:code 1026, :english "piece", :serbian "део" }
     ]))

