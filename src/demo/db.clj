(ns demo.db
  (:use [demo.db-config])
  (:require [clojure.java.jdbc :as jdbc]
            [utils]
            [clojure.string :only (trim upper-case replace) :as st]))

(def cached-schema nil)
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

(def p-join "LEFT OUTER JOIN rp_user ON rp_authors.user_id= rp_user.id")

(def o-join 
  (str "LEFT OUTER JOIN ams_pgm_asset_alignment " 
       "ON ams_pgm_asset_alignment.asset_id = ams_asset.asset_id "
       "LEFT OUTER JOIN ams_program ams_program_1 "
       "ON ams_pgm_asset_alignment.program_ref_id = ams_program_1.reference_id "
       "LEFT OUTER JOIN ams_wf_state_smy " 
       "ON ams_pgm_asset_alignment.asset_id = ams_wf_state_smy.asset_id "
       "LEFT OUTER JOIN ams_account " 
       "ON ams_program_1.account_id = ams_account.reference_id "
       "LEFT OUTER JOIN ams_pgm_hchy " 
       "ON ams_pgm_asset_alignment.program_ref_id = ams_pgm_hchy.subject_id "
       "LEFT OUTER JOIN ams_program ams_program_2 " 
       "ON ams_program_2.reference_id     = ams_pgm_hchy.relation_id"))

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
      (str keystr " = " (val i)))))

(defn
  create-coll
  [criteria]
  (println criteria)
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

;;;;;; need to remove 
(defn
  replace-prog
  [col]
  (doall
    (map 
      #(st/replace (st/upper-case %) "AMS_PROGRAM" "ams_program_1")
      col)))

(defn
  select-clause
  [output]
  (cl *SELECT* comma (replace-prog output)))

(defn
  from-clause
  [root]
  (cl *FROM* nil root))
  
(defn
  join-clause
  [tables]
  (when-not (utils/if-nil-or-empty tables)
    (str blank 
         (for [tbl tables]
           (str (cl *JOIN* nil (list tbl))
                (cl *JOIN-ON* nil (list tbl)))))))

(defn
  join-clause-temp
  [tables]
  (when-not (utils/if-nil-or-empty tables)
    (if (= db-type "postgres")
      (str blank p-join)
      (str blank o-join))))

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
  [output root tables criteria orderby]
  (str 
    (select-clause output)
    (from-clause root)
    (join-clause-temp tables)
    (where-clause criteria)
    (orderby-clause orderby)))

;;;;;;;;;;;;;; DATABASE METADATA ;;;;;;;;;;;;;;;;;;;

(defmacro 
  get-sql-metadata
  "Macro for getting DB Metadata"
  [db method & args] 
  `(jdbc/with-connection 
     ~db 
     (jdbc/transaction
       (doall
         (resultset-seq (-> 
                          (jdbc/connection)
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
  table-details
  [schm prefix]
  (into #{}
        (map 
          #(list (% :table_name) (% :column_name) (% :type_name))
          (get-columns schm prefix))))

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
        (map #(st/upper-case %) 
             proj_selected_tables))
      (fetch-table-columns-map 
        (db-attr :schema) 
        (db-attr :table_prefix))))

(defn 
  fetch-db-table-columns-map
  []
  (when (nil? cached-schema)
    (def cached-schema (fetch-schema-from-db))) 
  cached-schema)

;;;;;;;;;;;;;; TEST ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def poutput '("rp_user.id" 
                "rp_authors.first_name" 
                "rp_authors.last_name" 
                "rp_user.dept" 
                "rp_user.role"))
(def proot '("rp_authors"))
(def ptables '("rp_user"))
(def pcriteria '())
(def porderby '("rp_user.id"))

(def o-poutput '("ams_program.code" 
                "ams_account.name" 
                "ams_program.name" 
                "ams_program.name" 
                "ams_asset.asset_id"
                "ams_asset.asset_type"
                "ams_wf_state_smy.trh_ath"))
(def o-proot '("ams_asset"))
(def o-ptables '("ams_pgm_asset_alignment" "ams_program ams_program_1" "ams_wf_state_smy" "ams_account" "ams_pgm_hchy" "ams_program ams_program_2"))
(def o-pcriteria {:ams_wf_state_smy.activity_code  "TRH_TRH",
                  :ams_program_2.parent_id "IS NULL"})
(def o-porderby '("ams_asset.asset_id"))

(defn
  create-query-str-for-pg
  [op cr rt ord]
  (let [query 
        (st/trim 
          (generate-query-str 
            op 
            rt 
            ptables 
            cr 
            ord))]
    (println query)
    query))

(defn
  create-query-str-for-ora
  [op cr rt ord]
  (let [query 
        (st/trim 
          (generate-query-str 
            op 
            rt 
            o-ptables 
            cr 
            ord))]
    (println query)
    query))

(defn
  test-get-relations
  []
  (get-relationship 
    (db-attr :schema) 
    "AMS_ASSET" 
    "AMS_WF_STATE"))

(defn
  test-get-table-fk
  []
  (get-table-fk 
    (db-attr :schema) 
    "AMS_ASSET"))

(defn
  test-get-table-pk
  []
  (get-table-pk 
    (db-attr :schema) 
    "AMS_ASSET"))

(defn
  testpg-get-relations
  []
  (get-relationship 
    (db-attr :schema) 
    "rp_user" 
    "rp_authors"))

(defn
  testpg-get-table-fk
  []
  (get-table-fk 
    (db-attr :schema) 
    "rp_authors"))

(defn
  testpg-get-table-pk
  []
  (get-table-pk 
    (db-attr :schema) 
    "rp_user"))

;;;;;;;;;;;;;; DATABASE SANITY CHECK ;;;;;;;;;;;;;;;;;;;

(defn
  refresh-schema
  []
  (when-not (nil? cached-schema)
    (def cached-schema (fetch-db-table-columns-map))))

(defn
  sanity-check
  "for db sanity check"
  []
  (jdbc/with-connection (dbs)))
