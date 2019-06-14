(ns sample-server.person.entity
  (:require [language-lib.core :refer [get-label]]
            [sample-middle.person.entity :as smpe]
            [common-server.preferences :as prf])
  (:import [java.text SimpleDateFormat]))

(defn format-birthday-field
  "Formats birthday field into human readable format"
  [raw-birthday
   chosen-language]
  (when (and raw-birthday
             (instance?
               java.util.Date
               raw-birthday))
    (let [sdf (SimpleDateFormat.
              "d MMM yyyy")]
      (.format
        sdf
        raw-birthday))
   ))

(defn format-gender-field
  "Formats gender field into more human friandly format"
  [raw-gender
   chosen-language]
  (when (and raw-gender
             (string?
               raw-gender))
    (let [gender-a (atom raw-gender)]
      (when (= raw-gender
               smpe/gender-male)
        (reset!
          gender-a
          (get-label
            1018
            chosen-language))
       )
      (when (= raw-gender
               smpe/gender-female)
        (reset!
          gender-a
          (get-label
            1019
            chosen-language))
       )
      @gender-a))
 )

(defn reports
  "Returns reports projection"
  [request
   & [chosen-language]]
  (prf/set-preferences
    request)
  {:entity-label (get-label
                   1001
                   chosen-language)
   :projection [:first-name
                :last-name
                ;:email
                :birthday
                :height
                :weight
                :gender
                ;:diet
                ;:activity
                ]
   :qsort {:first-name 1}
   :rows (int
           (smpe/calculate-rows))
   :table-rows (int
                 @smpe/table-rows-a)
   :card-columns (int
                   @smpe/card-columns-a)
   :labels {:first-name (get-label
                          1002
                          chosen-language)
            :last-name (get-label
                         1003
                         chosen-language)
            :email (get-label
                     14
                     chosen-language)
            :height (get-label
                      1004
                      chosen-language)
            :weight (get-label
                      1005
                      chosen-language)
            :birthday (get-label
                        1006
                        chosen-language)
            :gender (get-label
                      1007
                      chosen-language)
            :diet (get-label
                      1008
                      chosen-language)
            :activity (get-label
                        1009
                        chosen-language)
            }
   :columns {:first-name {:width "30"
                          :header-background-color "lightblue"
                          :header-text-color "white"}
             :last-name {:width "30"
                         :header-background-color "lightblue"
                         :header-text-color "white"}
             :birthday {:width "35"
                        :header-background-color "lightblue"
                        :header-text-color "white"
                        :data-format-fn format-birthday-field
                        :column-alignment "C"}
             :height {:width "15"
                      :header-background-color "lightblue"
                      :header-text-color "white"
                      :column-alignment "R"}
             :weight {:width "15"
                      :header-background-color "lightblue"
                      :header-text-color "white"
                      :column-alignment "R"}
             :gender {:width "15"
                      :header-background-color "lightblue"
                      :header-text-color "white"
                      :data-format-fn format-gender-field
                      :column-alignment "C"}}
   })

