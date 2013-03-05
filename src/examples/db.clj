(ns examples.db
  (:require [clojure.java.jdbc :as jdbc]
            [utils]))

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
    (jdbc/insert-records :rp_authors
                     {:id 2 :first_name "Chas" :last_name "Emerick"}
                     {:id 3 :first_name "Christophe" :last_name "Grand"}
                     {:id 4 :first_name "Brian" :last_name "Carper"})))

(defn
  select-st
  "select statement example"
  []
  (jdbc/with-connection (dbs)
    (jdbc/with-query-results 
      res 
      ["SELECT * FROM AMS_WF_PROCESS_DEF"] ;AMS_WF_PROCESS_DEF / rp_user
      (doall res))))

