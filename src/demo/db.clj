(ns demo.db
  (:use [demo.db-config])
  (:require [clojure.java.jdbc :as jdbc]
            [utils]
            [clojure.string :only (trim upper-case replace) :as st]
            [demo.db-graph :as onjoin]))

(def cached-schema nil)
(def table-pk nil)
(def ^:dynamic *SELECT* "SELECT")
(def ^:dynamic *FROM* "FROM")
(def ^:dynamic *WHERE* "WHERE")
(def ^:dynamic *ORDER-BY* "ORDER BY")
(def ^:dynamic *JOIN* "JOIN")
(def ^:dynamic *JOIN-ON* "ON")
(def blank " ")
(def comma ", ")
(def -and " and ")
(def eqto " = ")
(def like " LIKE ")
(def uppercase " UPPER")
(def isnull " IS NULL ")
(def null-list (list isnull "NULL" "ISNULL" "NIL" "ISNIL"))
(def number-clm-types (list "numeric" "int" "int4" "number" "integer" "bigint" "smallint"))
(def proj_selected_tables ["ams_asset" "ams_program" "ams_wf_state_smy" "ams_account"])

(defn
  is-db-type-ora
  []
  (= db-type "oracle"))

(defn
  db-attr
  "getting db attribute"
  [mkey] 
  (get-in 
    db-types 
    [(keyword db-type) mkey]))

(defn 
  get-db-spec 
  "Create database specification map from inputs"
  [driver url proto user pw]
  {:classname driver
   :subprotocol proto
   :subname url
   :user user 
   :password pw})

(defn
  dbs
  []
  (get-db-spec 
     (db-attr :driver)
     (db-attr :url) 
     (db-attr :subproto)
     (db-attr :user) 
     (db-attr :pwd)))

(defn
  val-up
  [vl]
  (st/upper-case (st/trim vl)))

(defn
  clm-up
  [vl]
  (str uppercase "(" (st/trim vl) ")"))

(defn
  check-is-null
  [vl coll]
  (not-any? #(= (val-up (val vl)) (st/trim %)) coll))

(defn
  get-clm-type-name
  [t-c]
  (:type_name 
    (first 
      (filter (fn [mp] (= (mp :column_name) (second t-c))) 
              (-> (first t-c) cached-schema)))) )

(defn
  cr-alpha-numeric
  [i]
  (let [keystr (name (key i))
        t-c  (st/split keystr #"\.")
        typename (get-clm-type-name t-c)]
    (if (not-any? (fn [i] (= i typename)) number-clm-types)
      (str (clm-up keystr) like "'%" (val-up (val i)) "%' ")
      (if (number? (val i))
        (str keystr " = " (val i))
        (str keystr " = -1")))))

(defn
  create-coll
  [criteria]
  ;(println criteria)
  (map 
    (fn [i] (if-not (check-is-null i null-list) 
       (str (name (key i)) isnull)
       (cr-alpha-numeric i))) 
    criteria))

(defmacro
  cl
  [clause str coll]
  `(when-not (utils/if-nil-or-empty ~coll)
     (str blank ~clause blank
          (reduce 
            #(str %1 ~str %2) 
            ~coll))))

(defn
  select-clause
  [output]
  (cl *SELECT* comma output))

(defn
  from-clause
  [root]
  (cl *FROM* nil root))
  
(defn
  where-clause
  [criteria]
  (cl *WHERE* -and (create-coll criteria)))

(defn
  orderby-clause
  [orderby]
  (cl *ORDER-BY* comma orderby))

(defn
  generate-query-str
  "Generates the query string from UI values"
  [output root criteria orderby]
  (str 
    (select-clause output)
    (from-clause root)
    (onjoin/create-join (keyword root) output table-pk)
    (where-clause criteria)
    (orderby-clause orderby)))


(defn
  create-query-str
  [op cr rt ord]
  (let [query (st/trim (generate-query-str op rt cr ord))]
    (println query)
    query))

;;;;;;;;;;;;;; DATABASE METADATA ;;;;;;;;;;;;;;;;;;;

(defmacro 
  get-sql-metadata
  "Macro for getting DB Metadata"
  [db method & args] 
  `(jdbc/with-connection ~db 
     (jdbc/transaction 
       (doall
         (resultset-seq 
           (-> (jdbc/connection)
             (.getMetaData) 
             (~method ~@args)))))))

(defn
  get-tables
  "Get all Tables from database"
  [schm prefix]
  (get-sql-metadata 
    (dbs) 
    .getTables 
    nil schm (str prefix "%") (into-array ["TABLE"]))) ; "VIEW"

(defn
  get-columns
  "Get all columns from database"
  [schm prefix]
  (get-sql-metadata 
    (dbs) 
    .getColumns 
    nil schm (str prefix "%") nil))

(defn
  get-relationship
  "Get columns relationship as PK and FK"
  [schm parent-table foreign-table]
  (get-sql-metadata 
    (dbs) 
    .getCrossReference
    nil schm parent-table nil schm foreign-table))

(defn
  get-table-fk
  "Get FK column(s) of a table"
  [schm table]
  (get-sql-metadata 
    (dbs) 
    .getExportedKeys
    nil schm table))

(defn
  get-table-pk
  "Get PK column(s) of a table"
  [schm table]
  (get-sql-metadata 
    (dbs) 
    .getPrimaryKeys
    nil schm table))

(defn
  table-pk-map
  [schm tbl]
  (let [mp (first (get-table-pk schm tbl))]
    (hash-map (keyword (:table_name mp)) (:column_name mp))))

; Main function to get Table and its columns and Map (table [{clm1} {clm2} ... {clmn}])

(defn
  fetch-table-columns-map
  "Get Table and columns as map"
  [schm prefix]
  (into {}
        (group-by 
          :table_name 
          (get-columns schm prefix))))

(defn
  execute-query
  [query-str]
  (jdbc/with-connection (dbs)
    (jdbc/transaction 
      (jdbc/with-query-results 
        res 
        [query-str]
        (doall res)))))

(defn
  fetch-schema-from-db
  []
  (println "Getting DB Schema...")
  (if (is-db-type-ora)
      (select-keys 
        (fetch-table-columns-map 
          (db-attr :schema) 
          (db-attr :table_prefix))
        (map (fn [tb] (st/upper-case tb)) proj_selected_tables))
      (fetch-table-columns-map 
        (db-attr :schema) 
        (db-attr :table_prefix))))

(defn 
  fetch-db-table-columns-map
  []
  (when (nil? cached-schema)
    (def cached-schema (fetch-schema-from-db))) 
  cached-schema)

(defn
  create-pk-ralation
  [schm]
  (println "Creating PK Realations...")
  (apply merge 
         (map (fn [i] (table-pk-map (db-attr :schema) i)) (keys schm))))

(defn 
  get-pk-ralation
  "Creats a map of table : PK ralation"
  [schm]
  (when (nil? table-pk)
    (def table-pk (create-pk-ralation schm))))

;;;;;;;;;;;;;; DATABASE SANITY CHECK ;;;;;;;;;;;;;;;;;;;

(defn
  refresh-schema
  []
  (when (nil? cached-schema)
    (def cached-schema (fetch-schema-from-db))))

(defn
  sanity-check
  "for db sanity check"
  []
  (jdbc/with-connection (dbs)))

;;;;;;;;;;;;;; TEST ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro tst [fn & args]
  `(~fn (db-attr :schema) ~@args))

(defn test-get-relations []
  (if (is-db-type-ora)
    (tst get-relationship "AMS_ASSET" "AMS_WF_STATE")
    (tst get-relationship "rp_user" "rp_authors")))

(defn test-get-table-fk []
  (if (is-db-type-ora)
    (tst get-table-fk "AMS_ASSET")
    (tst get-table-fk "rp_user")))

(defn test-get-table-pk []
  (if (is-db-type-ora)
    (tst get-table-pk "AMS_ASSET")
    (tst get-table-pk "rp_user")))