(ns demo.action
  (:require [demo.db :as db]
            [clojure.string :only (capitalize replace-first) :as st]))
(def cached-schema nil)
(defn
  get-result
  [op cr]
  (if (= db/db-type "oracle")
    (db/execute-query 
      (if (utils/if-nil-or-empty op)
        (db/test-o-generate-query-str)
        (db/generate-o-query-str-only-op op cr)))
    (db/execute-query 
      (if (utils/if-nil-or-empty op)
        (db/test-generate-query-str)
        (db/generate-query-str-only-op op cr)))))

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
  (map #(st/capitalize (st/replace-first % prefix replaceby)) coll))

(defn
  get-header-clms
  [lst]
  (if (= db/db-type "oracle")
    (if (utils/if-nil-or-empty lst)
      (create-headers "ams_" "" db/o-poutput)
      (create-headers "ams_" "" lst))
    (if (utils/if-nil-or-empty lst)
      (create-headers "rp_" "" db/poutput)
      (create-headers "rp_" "" lst))))

