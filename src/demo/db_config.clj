(ns demo.db-config)

; Change following attrbute as per database
; valid values : postgres, oracle, mysql
(def db-type "postgres")

(def db-types {:oracle {
                        :type "oracle", 
                        :table_prefix "AMS", 
                        :schema "JAGDISH",
                        :driver "oracle.jdbc.driver.OracleDriver",
                        :subproto "oracle:thin",
                        :url "jdbc:oracle:thin:jagdish/jagdish@(DESCRIPTION=(ADDRESS = (PROTOCOL = TCP)(HOST =172.21.75.55)(PORT = 1522))(ADDRESS = (PROTOCOL = TCP)(HOST = 172.21.75.55)(PORT = 1522))(LOAD_BALANCE = yes)(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = amsdb)))",
                        :user "jagdish",
                        :pwd "jagdish"},
               :postgres {
                          :type "postgres", 
                          :table_prefix "rp", 
                          :schema "public",
                          :driver "org.postgresql.Driver",
                          :subproto "postgresql",
                          :url "//localhost:5432/postgres",
                          :user "postgres",
                          :pwd "postgres"},
               :mysql {
                       :type "mysql", 
                       :table_prefix "ams", 
                       :schema "ams_development",
                       :driver "com.mysql.jdbc.Driver",
                       :subproto "mysql",
                       :url "//172.21.76.79:3306/ams_development",
                       :user "ams_development",
                       :pwd "password"}})

(def meta_alias "MV_")

; Change this values as per your DB
(def rem-views '("AMS_ABSTRACT_ART_ASSET" "AMS_ABSTRACT_ASSET"))
(def rem-metadata '("AMS_METADATA_FIELD" "AMS_METADATA_VALUE"))
(def rem-clms '("REFERENCE_ID" "VERSION"))
(def metadata-value {:TABLE "AMS_METADATA_VALUE", :COLUMN "NAME", :CODE-CLM "CODE"})
(def no-mf-clms (list "CREATED_BY" "CREATED_DATE" "UPDATED_BY" "UPDATED_DATE" "VERSION" "REFERENCE_ID" "USER_VERSION"))
