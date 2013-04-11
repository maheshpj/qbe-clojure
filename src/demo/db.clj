(ns demo.db
  (:use [demo.db-config]
        [utils])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :only (trim upper-case replace) :as st]
            [demo.db-graph :as onjoin]))

(def cached-schema nil)
(def table-pk {:AMS_SEQUENCE_ATTRIBUTE "REFERENCE_ID", :AMS_USER_PERMISSION "REFERENCE_ID", :AMS_ADP_METADATA_FV "REFERENCE_ID", :AMS_TEST "REFERENCE_ID", :AMS_OTHER_ASSET "REFERENCE_ID", :AMS_RATIONALE_SET "REFERENCE_ID", :AMS_ART_INFO "REFERENCE_ID", :AMS_USER_ROLE_PERMISSION "USER_PERMISSION_ID", :AMS_USER_ACCOUNT_ROLE "MEMBER_OF_ROLE", :AMS_PGM_RATIONALE_SET "REFERENCE_ID", :AMS_ASSET_PAY_RATE "REFERENCE_ID", :AMS_STIMULUS_SPECIFICATION "REFERENCE_ID", :AMS_PGM_METADATA_FIELD "REFERENCE_ID", :AMS_STIMULUS_ALIGN_SCHEME "REFERENCE_ID", :AMS_ORDER_WF_STATE "REFERENCE_ID", :AMS_ORDER_METADATA_FV "REFERENCE_ID", :AMS_ASSET_ORDER_ALIGN "REFERENCE_ID", :AMS_WF_ACTIVITY_DEF "REFERENCE_ID", :AMS_USER "REFERENCE_ID", :AMS_EVENT "REFERENCE_ID", :AMS_COPYRIGHT "REFERENCE_ID", :AMS_HOTSPOT_AREA "REFERENCE_ID", :AMS_ADDRESS "REFERENCE_ID", :AMS_CONTENT_PART "REFERENCE_ID", :AMS_ART_ASSET "REFERENCE_ID", :AMS_ASSET_ORDER "REFERENCE_ID", :AMS_ASSET_DEV_PLAN "REFERENCE_ID", :AMS_AUTH_GRANT "REFERENCE_ID", :AMS_WF_ACTIVITY "REFERENCE_ID", :AMS_ASSET_ASSOCIATION "REFERENCE_ID", :AMS_SEQUENCE "REFERENCE_ID", :AMS_ASSESSMENT_ITEM "REFERENCE_ID", :AMS_PUBCONFIG_ASSET "REFERENCE_ID", :AMS_USER_ROLE_GROUP "MEMBER_OF_ROLE", :AMS_TYPE_QUANTITY "REFERENCE_ID", :AMS_STANDARD_SET "REFERENCE_ID", :AMS_ASSET_STATUS_VALUE "CODE", :AMS_LEVEL_GRADE_MAP "REFERENCE_ID", :AMS_TEI_EXTENSION "REFERENCE_ID", :AMS_STIMULUS_READABILITY_ALIGN "REFERENCE_ID", :AMS_ITEM_STANDARD_ALIGNMENT "REFERENCE_ID", :AMS_PGM_STANDARD_ALIGNMENT "REFERENCE_ID", :AMS_STANDARD "REFERENCE_ID", :AMS_ENTITY_LOCK "REFERENCE_ID", :AMS_ADP_DEV_YEAR "DEV_YEAR_CODE", :AMS_REPORTS "REFERENCE_ID", :AMS_PGM_METADATA_FIELD_VALUE "REFERENCE_ID", :AMS_WF_PROCESS_DEF "REFERENCE_ID", :AMS_ITEM_ALIGN_SCHEME "REFERENCE_ID", :AMS_TEST_FORM "REFERENCE_ID", :AMS_USER_ROLE "REFERENCE_ID", :AMS_ASSET "ASSET_ID", :AMS_ART_ASSET_FILE "REFERENCE_ID", :AMS_ASSET_CONTENT "REFERENCE_ID", :AMS_STIMULUS "REFERENCE_ID", :AMS_ASSET_TEMPLATE "REFERENCE_ID", :AMS_SEQUENCE_ELEMENT "REFERENCE_ID", :AMS_PROGRAM "REFERENCE_ID", :AMS_SEQ_REPORT "REFERENCE_ID", :AMS_HOTSPOT_EXTENSION "REFERENCE_ID", :AMS_WF_STATUS "STATUS_CODE", :AMS_PGM_HCHY "RELATION_ID", :AMS_WF_ACTIVITY_STATUS "ACTIVITY_REFID", :AMS_WF_TRANSITION_RULE "REFERENCE_ID", :AMS_HOTSPOT_CHOICE "REFERENCE_ID", :AMS_ASSET_FILE "REFERENCE_ID", :AMS_WF_ACTIVITY_PERM "USER_PERMISSION_REFID", :AMS_ACCOUNT "REFERENCE_ID", :AMS_OTHER_ASSET_FV "REFERENCE_ID", :AMS_ORDER_CONTRACT "REFERENCE_ID", :AMS_ITEM_TOOLS "REFERENCE_ID", :AMS_REPORTING_CATEGORY "REFERENCE_ID", :AMS_ORDER_DETAIL "REFERENCE_ID", :AMS_WF_STATE_SMY "ASSET_ID", :AMS_WF_STATE "REFERENCE_ID", :AMS_PGM_ASSET_ALIGNMENT "REFERENCE_ID"})
(def db-grph nil)

(def ^:dynamic *SELECT* "SELECT")
(def ^:dynamic *FROM* "FROM")
(def ^:dynamic *WHERE* "WHERE")
(def ^:dynamic *ORDER-BY* "ORDER BY")
(def ^:dynamic *GROUP-BY* "GROUP BY") 
(def blank " ")
(def comma ", ")
(def -and " AND ")
(def eqto " = ")
(def like " LIKE ")
(def uppercase " UPPER")
(def isnull " IS NULL ")
(def isnotnull " IS NOT NULL ")
(def null-list (list isnull "NULL" "ISNULL" "NIL" "ISNIL"))
(def not-null-list (list isnotnull "NOTNULL" "NOT NULL" "NOT NIL" "IS NOT NIL"))
(def number-clm-types (list "numeric" "int" "int4" "NUMBER" "integer" "bigint" "smallint"))
(def number-symbols (list ">" "<" "=" ">=" "<=" "!=" "<>"))


(defn
  dotstr
  [kee]
  (str "." (kee metadata-value)))

(defn 
  from-meta-tbl
  [mf]
  (str " LEFT OUTER JOIN " (:TABLE metadata-value) " "
       (st/replace mf (dotstr :COLUMN) "")))

(defn
  on-meta-tbl
  [mf tbclm]
  (str " ON " (st/replace mf (dotstr :COLUMN) (dotstr :CODE-CLM)) eqto tbclm))

(defn
  meta-join
  [mf tbclm]
  (str (from-meta-tbl mf) (on-meta-tbl mf tbclm)))

(defn
  is-db-type-ora
  []
  (= db-type "oracle"))

(defn
  db-attr
  "getting db attribute"
  [mkey] 
  (get-in db-types [(keyword db-type) mkey]))

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
  parenthise
  [vl]
  (str "(" vl ")"))

(defn
  clm-up
  [vl]
  (str uppercase (parenthise (st/trim vl))))

(defn
  get-clm-type-name
  [t-c]
  (:type_name 
    (first 
      (filter (fn [mp] (= (mp :column_name) (second t-c))) 
              (-> (first t-c) cached-schema)))))

(defn
  single-crit-num
  [keystr valstr]
  (if (some #(.startsWith (st/trim valstr) %) number-symbols)
    (str keystr valstr)
    (if (number? (Integer. valstr)) (str keystr " = " valstr))))


(defn
  split-num-and-vals
  [keystr valstr]
  (let [vls (st/split (st/upper-case valstr) #"AND")]
    (map #(single-crit-num keystr %) vls)))

(defn
  process-number
  [keystr valstr]
  (apply str (interpose -and (split-num-and-vals keystr valstr))))

(defn
  single-crit-string
  [keystr valstr]
  (str (clm-up keystr) like "'%" (val-up valstr) "%' "))

(defn
  split-and-vals
  [keystr valstr]
  (let [vls (st/split (st/upper-case valstr) #"AND")]
    (map #(single-crit-string keystr %) vls)))

(defn
  process-string
  [keystr valstr]
  (apply str (interpose -and (split-and-vals keystr valstr))))

(defn
  cr-alpha-numeric
  [i]
  (let [keystr (name (key i))
        valstr (val i)
        t-c  (st/split keystr #"\.")
        typename (get-clm-type-name t-c)]
    (if (some (fn [i] (= i typename)) number-clm-types)
      (process-number keystr valstr)
      (process-string keystr valstr))))

(defn
  find-in-coll
  [vl coll]
  (some #(= (val-up (st/trim vl)) (st/trim %)) coll))

(defn 
  process-cr
  [i]
  (let [kee (name (key i))
        vl (val i)]
    (if (find-in-coll vl null-list) 
      (str kee isnull)
      (if (find-in-coll vl not-null-list)
        (str kee isnotnull)
        (cr-alpha-numeric i)))))

(defn
  create-coll
  [criteria]
  (map (fn [i] (process-cr i)) criteria))

(defmacro
  cl
  [clause str coll]
  `(when-not (utils/if-nil-or-empty ~coll)
     (str blank ~clause blank
          (reduce #(str %1 ~str %2) ~coll))))

(defn
  replace-grp-clm
  [clm groupby]
  (if (= clm (name (key groupby))) 
      (st/replace clm clm (str (val groupby) (parenthise clm)))
      clm))

(defn
  grpby-select
  [groupby output]
  (map #(replace-grp-clm % groupby) output))

(defn
  select-clause
  [output groupby]
  (if (if-nil-or-empty groupby)
    (cl *SELECT* comma output)
    (cl *SELECT* comma (grpby-select groupby output))))

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
  groupby-clause
  [groupby]
  (cl *GROUP-BY* comma groupby))

(defn
  clm-alias
  [revmp i]
  (let [nm (name (get revmp i))]
    (subs nm (+ 1 (.indexOf nm ".")) (count nm))))

(defn
  select-alias
  [ch-op meta]
  (let [revmp (zipmap (vals meta) (keys meta))]
    (map (fn [i] (if (contains? revmp i) 
                   (st/replace i i (str i " AS \"" (clm-alias revmp i) "\""))
                   i)) ch-op)))

(defn
  generate-query-str
  "Generates the query string from UI values"
  [output ch-op root ch-cr ch-ord ch-grp meta]
  (str 
    (select-clause (select-alias ch-op meta) ch-grp)
    (from-clause root)
    (onjoin/create-join db-grph (keyword root) output table-pk)
    (when-not (if-nil-or-empty meta) 
      (apply str (map #(meta-join (val %) (name (key %))) meta)))
    (where-clause ch-cr)
    (if (and (> (count ch-op) 1) (not (if-nil-or-empty ch-grp)))
      (groupby-clause (remove #(= % (name (key ch-grp))) ch-op)))
    (orderby-clause ch-ord)))

(defn
  create-query-str
  [op ch-op cr ch-cr rt ord ch-ord grp ch-grp mf]
  (let [query (st/trim (generate-query-str op ch-op rt ch-cr ch-ord ch-grp mf))]
    (println query) query))

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
  (get-sql-metadata (dbs) 
                    .getTables 
                    nil schm (str prefix "%") (into-array ["TABLE"]))) ; "VIEW"

(defn
  get-columns
  "Get all columns from database"
  [schm prefix]
  (get-sql-metadata (dbs) 
                    .getColumns 
                    nil schm (str prefix "%") nil))

(defn
  get-relationship
  "Get columns relationship as PK and FK"
  [schm parent-table foreign-table]
  (get-sql-metadata (dbs) 
                    .getCrossReference
                    nil schm parent-table nil schm foreign-table))

(defn
  get-table-fk
  "Get FK column(s) of a table"
  [schm table]
  (get-sql-metadata (dbs) 
                    .getExportedKeys
                    nil schm table))

(defn
  get-table-pk
  "Get PK column(s) of a table"
  [schm table]
  (get-sql-metadata (dbs) 
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
  (into {} (group-by :table_name (get-columns schm prefix))))

(defn
  execute-query
  [query-str]
  (jdbc/with-connection (dbs)
    (jdbc/transaction 
      (jdbc/with-query-results 
        res [query-str] (doall res)))))

(defn
  fetch-schema-from-db
  []
  (println "Getting DB Schema...")
  (apply dissoc 
         (fetch-table-columns-map 
           (db-attr :schema) 
           (db-attr :table_prefix))
         (vec (concat rem-views rem-metadata))))

(defn 
  fetch-db-table-columns-map
  []
  (when (nil? cached-schema)
    (def cached-schema (fetch-schema-from-db))) 
  cached-schema)

(defn
  set-util-prf
  []
  (set-prf (str (db-attr :table_prefix) "_")))

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

;;;;;;;;;;;;;;;;;;;;;;;;;  DB TABLE GRAPH  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn
  all-table-fk
  []
  (map #(into {} (get-table-fk (db-attr :schema) %)) (keys cached-schema)))

(defn 
  get-tbl-graph 
  [] 
  (map #(vals (select-keys % db-grph-keys)) (filter not-empty (all-table-fk))))

(defn
  edge
  [coln]
  (map #(keyword %) coln))

(defn 
  get-db-graph
  []
  (println "Creating DB Tables Graph...")
  (let [dbgrp (get-tbl-graph)]
    (map #(vec (reverse (merge (reverse (edge (butlast %))) (last %)))) dbgrp)))

(defn
  create-db-graph
  []
  (when (nil? db-grph)
    (def db-grph ;ams-graph)));
      (get-db-graph))))

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