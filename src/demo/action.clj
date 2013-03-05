(ns demo.action
  (:require [demo.db :as db]
            [clojure.string :only (capitalize replace-first) :as st]))

(defn
  get-result
  []
  (if (= db/db-type "oracle")
    (db/execute-query db/test-oracle-query)
    (db/execute-query (db/test-generate-query-str))))

(defn
  get-schema
  []
  (db/fetch-db-table-columns-map))

(defn
  get-header-clms
  []
  (if (= db/db-type "oracle")
    ["Project Id" "Account" "Program" "Project" "Asset ID" "Asset Type" "ATH"]
    (map #(st/capitalize (st/replace-first % "rp_" "")) db/poutput)))

