(ns demo.action
  (:require [demo.db :as db]))

(defn
  get-result
  []
  (if (= db/db-type "oracle")
    (db/execute-query db/test-oracle-query)
    (db/execute-query db/test-pg-query)))

(defn
  get-schema
  []
  (db/fetch-db-table-columns-map))

(defn
  get-header-clms
  []
  (if (= db/db-type "oracle")
    ["Project Id" "Account" "Program" "Project" "Asset ID" "Asset Type" "ATH"]
    ["UserID" "First Name" "Last Name" "Dept" "Role"]))

