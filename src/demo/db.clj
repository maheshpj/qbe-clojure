(ns demo.db
  (:require [clojure.java.jdbc :as jdbc]
            [utils]
            [clojure.string :only (trim) :as st]))

; Change following attrbute as per database
; valid values : postgres, oracle, mysql
(def db-type "postgres")

(def db-types {:oracle {
                        :type "oracle", 
                        :table_prefix "AMS", 
                        :schema "JAGDISH"
                        :driver "oracle.jdbc.driver.OracleDriver"
                        :subproto "oracle:thin"
                        :url "jdbc:oracle:thin:jagdish/jagdish@(DESCRIPTION=(ADDRESS = (PROTOCOL = TCP)(HOST =172.21.75.55)(PORT = 1522))(ADDRESS = (PROTOCOL = TCP)(HOST = 172.21.75.55)(PORT = 1522))(LOAD_BALANCE = yes)(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = amsdb)))"
                        :user "jagdish"
                        :pwd "jagdish"},
               :postgres {
                          :type "postgres", 
                          :table_prefix "rp", 
                          :schema "public"
                          :driver "org.postgresql.Driver"
                          :subproto "postgresql"
                          :url "//localhost:5432/postgres"
                          :user "postgres"
                          :pwd "postgres"},
               :mysql {
                       :type "mysql", 
                       :table_prefix "", 
                       :schema ""
                       :driver ""
                       :subproto ""
                       :url ""
                       :user ""
                       :pwd ""}})
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

(def test-oracle-query 
  "SELECT p.reference_id proj_id,
		  act.name,
		  prg.name prog,
		  p.name proj,
		  ast.asset_id,
		  ast.asset_type,
		  smy.trh_ath author
		FROM ams_asset ast
		  JOIN ams_pgm_asset_alignment paa
		    ON paa.asset_id = ast.asset_id
		  JOIN ams_program p
		    ON paa.program_ref_id = p.reference_id
		  JOIN ams_wf_state_smy smy
		    ON paa.asset_id = smy.asset_id
		  JOIN ams_account act
		    ON p.account_id = act.reference_id
		  JOIN ams_pgm_hchy h
		    ON paa.program_ref_id = h.subject_id
		  JOIN ams_program prg
		    ON prg.reference_id     = h.relation_id
		WHERE 
		  smy.activity_code = 'TRH_TRH'
		  AND ( smy.trh_trh      IS NULL
		    OR smy.trh_trh         != 'R')
		  AND prg.parent_id      IS NULL")

(def test-pg-query 
  "SELECT usr.id, ath.first_name, ath.last_name, usr.dept, usr.role 
   From rp_authors ath
     LEFT OUTER JOIN rp_user usr
       ON ath.user_id= usr.id")

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
  (into {} 
    {:classname driver
     :subprotocol proto
     :subname url
     :user user 
     :password pw}))

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
  create-coll
  [criteria]
  (map 
    #(str (name (key %)) eqto (name (val %))) 
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
    (str blank "LEFT OUTER JOIN rp_user ON rp_authors.user_id= rp_user.id")))

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
     (doall
           (resultset-seq (-> 
                            (jdbc/connection)
                            (.getMetaData) 
                            (~method ~@args))))))

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
    (jdbc/with-query-results 
      res 
      [query-str]
      (doall res))))

;;;;;;;;;;;;;; TEST ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def poutput '("rp_user.id" "rp_authors.first_name" "rp_authors.last_name" "rp_user.dept" "rp_user.role"))
(def proot '("rp_authors"))
(def ptables '("rp_user"))
(def pcriteria '())
(def porderby '("rp_user.id"))

(defn
  test-generate-query-str
  []
  (st/trim (generate-query-str poutput proot ptables pcriteria porderby)))

(defn
  generate-query-str-only-op
  [op]
  (st/trim (generate-query-str op proot ptables pcriteria porderby)))

(defn 
  fetch-db-table-columns-map
  []
  (fetch-table-columns-map 
    (db-attr :schema) 
    (db-attr :table_prefix)))

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
  sanity-check
  "for db sanity check"
  []
  (jdbc/with-connection (dbs)))
