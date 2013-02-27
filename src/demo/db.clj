(ns demo.db
  (:require [clojure.java.jdbc :as jdbc]))

(def o-driver "oracle.jdbc.driver.OracleDriver")
(def o-url "jdbc:oracle:thin:jagdish/jagdish@(DESCRIPTION=(ADDRESS = (PROTOCOL = TCP)(HOST =172.21.75.55)(PORT = 1522))(ADDRESS = (PROTOCOL = TCP)(HOST = 172.21.75.55)(PORT = 1522))(LOAD_BALANCE = yes)(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = amsdb)))")
(def o-user "jagdish")
(def o-pwd "jagdish")
(def db-types {:oracle "oracle" :mysql "MySql"})
(def cur-db-type (:oracle db-types)) ; what is the currect db type? 

(defn 
  get-db-spec 
  "Create database specification map from inputs"
  [driver url user pw]
  (let [url-parts (.split #":" url)]
    {:classname driver
     :subprotocol (str (nth url-parts 1) ":" (nth url-parts 2))
     :subname url
     :user user 
     :password pw }))

(defn 
  oracle-db-spec
  "Get Oracle driver db spec"
  []
  (get-db-spec o-driver o-url o-user o-pwd))

(defn
  dbs
  []
  (if (= cur-db-type (:oracle db-types))  
    (oracle-db-spec)))

(defn
  create-st
  "create statement example"
  []
  (jdbc/with-connection (dbs)
    (jdbc/create-table :authors
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
      ["SELECT * FROM AMS_WF_PROCESS_DEF"]
      (doall res))))


(defn
  sanity-check
  "for db sanity check"
  []
  (jdbc/with-connection (dbs)))
