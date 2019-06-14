(ns sample-server.preferences
  (:require [sample-middle.person.entity :as smpe]))

(defn set-specific-preferences-fn
  "Sets preferences on server side"
  [specific-map]
  (let [{{{table-rows-p :table-rows
           card-columns-p :card-columns} :person-entity} :display} specific-map]
    (reset!
      smpe/table-rows-a
      (or table-rows-p
          10))
    (reset!
      smpe/card-columns-a
      (or card-columns-p
          0))
   ))

