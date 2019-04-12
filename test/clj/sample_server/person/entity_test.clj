(ns sample-server.person.entity-test
  (:require [clojure.test :refer :all]
            [sample-server.person.entity :refer :all]
            [mongo-lib.core :as mon])
  (:import [java.text SimpleDateFormat]
           [java.util Calendar
                      Date
                      TimeZone]))

(def db-uri
     (or (System/getenv "MONGODB_URI")
         (System/getenv "PROD_MONGODB")
         "mongodb://admin:passw0rd@127.0.0.1:27017/admin"))

(def db-name
     "test-db")

(defn create-db
  "Create database for testing"
  []
  (mon/mongodb-connect
    db-uri
    db-name)
  (mon/mongodb-insert-many
    "language"
    [{ :code 1018
       :english "Male"
       :serbian "Мушки" }
     { :code 1019
       :english "Female"
       :serbian "Женски" }]))

(defn destroy-db
  "Destroy testing database"
  []
  (mon/mongodb-drop-database
    db-name)
  (mon/mongodb-disconnect))

(deftest test-format-birthday-field
  (testing "Test format birthday field"
    
    (let [raw-birthday nil
          chosen-language nil
          result (format-birthday-field
                   raw-birthday
                   chosen-language)]
      
      (is
        (nil?
          result)
       )
      
     )
    
    (let [date (Calendar/getInstance)
          void (.set
                 date
                 Calendar/DATE
                 11)
          void (.set
                 date
                 Calendar/MONTH
                 10)
          void (.set
                 date
                 Calendar/YEAR
                 2013)
          void (.setTimeZone
                 date
                 (TimeZone/getDefault))
          raw-birthday (Date.
                         (.getTimeInMillis
                           date))
          chosen-language nil
          result (format-birthday-field
                   raw-birthday
                   chosen-language)]
      
      (is
        (not
          (nil?
            result))
       )
      
      (is
        (string?
          result)
       )
      
      (is
        (= result
           "11 Nov 2013")
       )
      
     )
    
   ))

(deftest test-format-gender-field
  (testing "Test format gender field"
    
    (create-db)
    
    (let [raw-gender nil
          chosen-language nil
          result (format-gender-field
                   raw-gender
                   chosen-language)]
      
      (is
        (nil?
          result)
       )
      
     )
    
    (let [raw-gender "unknown"
          chosen-language nil
          result (format-gender-field
                   raw-gender
                   chosen-language)]
      
      (is
        (not
          (nil?
            result))
       )
      
      (is
        (= result
           "unknown")
       )
      
     )
    
    (let [raw-gender "male"
          chosen-language nil
          result (format-gender-field
                   raw-gender
                   chosen-language)]
      
      (is
        (not
          (nil?
            result))
       )
      
      (is
        (= result
           "Male")
       )
      
     )
    
    (let [raw-gender "female"
          chosen-language nil
          result (format-gender-field
                   raw-gender
                   chosen-language)]
      
      (is
        (not
          (nil?
            result))
       )
      
      (is
        (= result
           "Female")
       )
      
     )
    
    (let [raw-gender "male"
          chosen-language "serbian"
          result (format-gender-field
                   raw-gender
                   chosen-language)]
      
      (is
        (not
          (nil?
            result))
       )
      
      (is
        (= result
           "Мушки")
       )
      
     )
    
    (let [raw-gender "female"
          chosen-language "serbian"
          result (format-gender-field
                   raw-gender
                   chosen-language)]
      
      (is
        (not
          (nil?
            result))
       )
      
      (is
        (= result
           "Женски")
       )
      
     )
    
    (destroy-db)
    
   ))


