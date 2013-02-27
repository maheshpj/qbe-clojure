(ns demo.db
  (:require [clojure.java.jdbc :as jdbc]))

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

(def db-types {:oracle "oracle" :mysql "mysql" :postgres "postgres"})
(def cur-db-type (:postgres db-types)) ; what is the currect db type? 
(def table-prefix "rp")

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
  (if (= cur-db-type (:oracle db-types))  
    (oracle-db-spec)
    (if (= cur-db-type (:postgres db-types))
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
      ["SELECT * FROM rp_user"]
      (doall res))))

;;;;;;;;;;;;;; DATABASE METADATA ;;;;;;;;;;;;;;;;;;;

(defmacro 
  get-sql-metadata
  [db method & args] 
  `(jdbc/with-connection 
     ~db 
     (resultset-seq (-> 
                      (jdbc/connection)
                      (.getMetaData) 
                      (~method ~@args)))))

(defn
  get-tables
  "Get all Tables from database"
  []
  (get-sql-metadata 
    (dbs) 
    .getTables 
    nil nil nil (into-array ["TABLE" "VIEW"])))

(defn
  get-columns
  "Get all columns from database"
  []
  (get-sql-metadata 
    (dbs) 
    .getColumns 
    nil nil nil nil))

(defn
  table-details
  []
  (into #{}
        (map 
          #(list (% :table_name) (% :column_name) (% :type_name))
          (get-columns))))

(defn
  get-table-details
  "Take database spec, return all column names from the database metadata"
  [prefix]
  (filter 
    #(.startsWith (first %) prefix) 
    (table-details)))

(def
  table-set
  "Set of all tables"
  (map :table_name (get-tables)))

;;;;;;;;;;;;;; DATABASE SANITY CHECK ;;;;;;;;;;;;;;;;;;;

(defn
  sanity-check
  "for db sanity check"
  []
  (jdbc/with-connection (dbs)))
