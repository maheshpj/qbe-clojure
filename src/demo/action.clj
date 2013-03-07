(ns demo.action
  (:require [demo.db :as db]
            [clojure.string :only (capitalize replace-first upper-case) :as st]))

(def cached-schema nil)

(defn
  get-result
  [op cr]
  (if (db/is-db-type-ora)
    (db/execute-query 
      (if (utils/if-nil-or-empty op)
        (db/create-query-str-for-ora)
        (db/create-query-str-for-ora op cr)))
    (db/execute-query 
      (if (utils/if-nil-or-empty op)
        (db/create-query-str-for-pg)
        (db/create-query-str-for-pg op cr)))))

(defn
  get-schema
  []
  (if (nil? cached-schema)
    (def cached-schema (db/fetch-db-table-columns-map))
    cached-schema)
  cached-schema)

(defn
  create-headers
  [prefix replaceby coll]
  (map #(st/capitalize 
          (st/replace-first 
            (st/upper-case %) prefix replaceby)) 
       coll))

(defn
  get-header-clms
  [lst]
  (if (db/is-db-type-ora)
    (if (utils/if-nil-or-empty lst)
      (create-headers "AMS_" "" db/o-poutput)
      (create-headers "AMS_" "" lst))
    (if (utils/if-nil-or-empty lst)
      (create-headers "RP_" "" db/poutput)
      (create-headers "RP_" "" lst))))

