(ns demo.action
  (:require [demo.db :as db]
            [clojure.string :only (capitalize replace-first) :as st]))

(defn
  get-result
  [lst]
  (if (= db/db-type "oracle")
    (db/execute-query db/test-oracle-query)
    (db/execute-query 
      (if (utils/if-nil-or-empty lst)
        (db/test-generate-query-str)
        (db/generate-query-str-only-op lst)))))

(defn
  get-schema
  []
  (db/fetch-db-table-columns-map))

(defn
  get-header-clms
  [lst]
  (if (= db/db-type "oracle")
    ["Project Id" "Account" "Program" "Project" "Asset ID" "Asset Type" "ATH"]
    (if (utils/if-nil-or-empty lst)
      (map #(st/capitalize (st/replace-first % "rp_" "")) db/poutput)
      (map #(st/capitalize (st/replace-first % "rp_" "")) lst))))

