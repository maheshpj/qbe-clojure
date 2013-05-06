(ns demo.db-config)

; Change following attrbute as per database
; valid values : postgres, oracle, mysql
(def db-type "oracle")

(def db-types {:oracle {
                        :type "oracle", 
                        :table_prefix "", 
                        :schema "",
                        :driver "oracle.jdbc.driver.OracleDriver",
                        :subproto "oracle:thin",
                        :url "",
                        :user "",
                        :pwd ""},
               :postgres {
                          :type "postgres", 
                          :table_prefix "", 
                          :schema "",
                          :driver "org.postgresql.Driver",
                          :subproto "postgresql",
                          :url "",
                          :user "",
                          :pwd ""},
               :mysql {
                       :type "mysql", 
                       :table_prefix "", 
                       :schema "",
                       :driver "com.mysql.jdbc.Driver",
                       :subproto "mysql",
                       :url "",
                       :user "",
                       :pwd ""}})

(def meta_alias "MV_")

; Change this values as per your DB
(def rem-views '())
(def rem-metadata '())
(def rem-clms '())
(def metadata-value {:TABLE "", :COLUMN "", :CODE-CLM ""})
(def no-mf-clms (list ""))