(ns demo.db
  (:use [demo.db-config]
        [utils])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :only (trim upper-case replace) :as st]
            [demo.db-graph :as onjoin]))

(def cached-schema nil)
(def table-pk nil)
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
  create-metatblname
  [mf]
  (str (st/upper-case (str (st/replace mf " " "-") "_MDV"))))

(defn 
  from-meta-tbl
  [mf]
  (str " LEFT OUTER JOIN"
       " (SELECT AMS_METADATA_FIELD.NAME FIELD_NAME, AMS_METADATA_VALUE.REFERENCE_ID, AMS_METADATA_VALUE.CODE, AMS_METADATA_VALUE.NAME" 
       " FROM AMS_METADATA_FIELD, AMS_METADATA_VALUE"
       " WHERE AMS_METADATA_VALUE.METADATA_FIELD_ID = AMS_METADATA_FIELD.REFERENCE_ID"
       " AND AMS_METADATA_FIELD.NAME = " mf
			    ")" (create-metatblname mf)))

(defn
  on-meta-tbl
  [mf tbclm]
  (str (create-metatblname mf) ".CODE" eqto tbclm))

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
  replace-meta-clm
  [clm meta]
  (println "clm: " clm)
  (if (some #(= % clm) (map name (keys meta)))
    (st/replace clm clm (str (create-metatblname ((keyword clm) meta)) ".NAME"))
    clm))

(defn
  meta-out
  [meta output]
  (map #(replace-meta-clm % meta) output))

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
  generate-query-str
  "Generates the query string from UI values"
  [output root criteria orderby groupby meta]
  (when-not (if-nil-or-empty meta) 
    (def output (meta-out meta output)))
  (println output)
  (str 
    (select-clause output groupby)
    (from-clause root)
    (onjoin/create-join db-grph (keyword root) output table-pk)
    (when-not (if-nil-or-empty meta) 
      (map #(meta-join (val %) (name (key %))) meta))
    (where-clause criteria)
    (if (and (> (count output) 1) (not (if-nil-or-empty groupby)))
      (groupby-clause (remove #(= % (name (key groupby))) output)))
    (orderby-clause orderby)))

(defn
  create-query-str
  [op cr rt ord grp mf]
  (let [query (st/trim (generate-query-str op rt cr ord grp mf))]
    (println query)
    query))

(defn
  matadata-fields
  "get all metadata fields"
  []
  (let [mf (first metadata-fields)]
    (jdbc/with-connection (dbs)
      (jdbc/with-query-results 
        res 
        [(str "SELECT " (val mf) " FROM " (key mf))]
        (doall res)))))

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
    (def db-grph ams-graph)));
      ;(get-db-graph))))

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