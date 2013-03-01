(ns demo.db
  (:require [clojure.java.jdbc :as jdbc]))

; Change following attrbute as per database
; valid values : postgres, oracle, mysql
(def db-type "postgres")

; Oracle
(def o-driver "oracle.jdbc.driver.OracleDriver")
(def o-url "jdbc:oracle:thin:jagdish/jagdish@(DESCRIPTION=(ADDRESS = (PROTOCOL = TCP)(HOST =172.21.75.55)(PORT = 1522))(ADDRESS = (PROTOCOL = TCP)(HOST = 172.21.75.55)(PORT = 1522))(LOAD_BALANCE = yes)(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = amsdb)))")
(def o-subproto "oracle:thin")
(def o-user "jagdish")
(def o-pwd "jagdish")

; Postgres
(def p-driver "org.postgresql.Driver")
(def p-subproto "postgresql")
(def p-url "//localhost:5432/postgres")
(def p-user "postgres")
(def p-pwd "postgres")

(def db-types {:oracle {:type "oracle", :table_prefix "AMS", :schema "JAGDISH"},
               :postgres {:type "postgres", :table_prefix "rp", :schema "public"},
               :mysql {:type "mysql", :table_prefix "", :schema ""}})

(def cur-db-type (get-in db-types [(keyword db-type) :type])) 

(defn 
  get-db-spec 
  "Create database specification map from inputs"
  [driver url proto user pw]
  (let [url-parts (.split #":" url)]
    {:classname driver
     :subprotocol proto
     :subname url
     :user user 
     :password pw }))

(defn 
  oracle-db-spec
  "Get Oracle driver db spec"
  []
  (get-db-spec o-driver o-url o-subproto o-user o-pwd))

(defn 
  postgres-db-spec
  "Get Postgres driver db spec"
  []
  (get-db-spec p-driver p-url p-subproto p-user p-pwd))

(defn
  dbs
  []
  (if (= 
        (get-in db-types [(keyword cur-db-type) :type]) 
        (get-in db-types [:oracle :type]))  
    (oracle-db-spec)
    (if (= 
          (get-in db-types [(keyword cur-db-type) :type]) 
          (get-in db-types [:postgres :type]))
      (postgres-db-spec))))

(defn
  create-st
  "create statement example"
  []
  (jdbc/with-connection (dbs)
    (jdbc/create-table :rp_authors
                       [:id "integer primary key"]
                       [:first_name "varchar"]
                       [:last_name "varchar"])))

(defn
  insert-st
  "insert statement example"
  []
  (jdbc/with-connection (dbs)
    (jdbc/insert-records :authors
                     {:first_name "Chas" :last_name "Emerick"}
                     {:first_name "Christophe" :last_name "Grand"}
                     {:first_name "Brian" :last_name "Carper"})))

(defn
  select-st
  "select statement example"
  []
  (jdbc/with-connection (dbs)
    (jdbc/with-query-results 
      res 
      ["SELECT * FROM AMS_WF_PROCESS_DEF"] ;AMS_WF_PROCESS_DEF / rp_user
      (doall res))))

(defn
  get-oracle-metadata
  []
  (jdbc/with-connection (dbs)
    (jdbc/with-query-results 
      res 
      ["select *
        from all_constraints c
          inner join all_constraints cc 
            on c.r_constraint_name = cc.constraint_name
        where c.table_name like 'AMS%'
          and c.table_name = 'AMS_ASSET'"] 
      (doall res))))


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
        (group-by :table_name (get-columns schm prefix))))

;;;;;;;;;;;;;; TEST ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn 
  test-fetch-table-columns-map
  []
  (fetch-table-columns-map 
    (get-in db-types [(keyword cur-db-type) :schema]) 
    (get-in db-types [(keyword cur-db-type) :table_prefix])))

(defn
  test-get-relations
  []
  (get-relationship 
    (get-in db-types [(keyword cur-db-type) :schema]) 
    "AMS_ASSET" 
    "AMS_WF_STATE"))

(defn
  test-get-table-fk
  []
  (get-table-fk 
    (get-in db-types [(keyword cur-db-type) :schema]) 
    "AMS_ASSET"))

(defn
  test-get-table-pk
  []
  (get-table-pk 
    (get-in db-types [(keyword cur-db-type) :schema]) 
    "AMS_ASSET"))

;;;;;;;;;;;;;; DATABASE SANITY CHECK ;;;;;;;;;;;;;;;;;;;

(defn
  sanity-check
  "for db sanity check"
  []
  (jdbc/with-connection (dbs)))
