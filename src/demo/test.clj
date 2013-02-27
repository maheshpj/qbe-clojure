(ns demo.test)

(def url "jdbc:oracle:thin:jagdish/jagdish@(DESCRIPTION=(ADDRESS = (PROTOCOL = TCP)(HOST =172.21.75.55)(PORT = 1522))(ADDRESS = (PROTOCOL = TCP)(HOST = 172.21.75.55)(PORT = 1522))(LOAD_BALANCE = yes)(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = amsdb)))")
(def k '(3 (1 g) (4 b)))

(defn
  split
  []
  (let [parts (.split #":" url)]
    (str (nth parts 1) ":" (nth parts 2))))

(defn
  getrest2 
  [restlst simplerestlst]
  (if (or (nil? restlst) (empty? restlst))
    (reverse simplerestlst)
    (getrest2 
      (rest restlst)
      (cons 
        (ffirst restlst) 
        simplerestlst))))

(defn
  getrest
  [restlst]
  (getrest2 restlst nil))

(defn 
  get-simple-list
  [list]
  (cons (first list) (getrest (rest list))))

(def lst '(("rp_user" "role" "text") ("rp_user" "last_name" "text") ("rp_authors" "first_name" "varchar") ("rp_authors" "last_name" "varchar") ("rp_authors" "id" "int4") ("rp_user" "dept" "numeric") ("rp_user" "id" "numeric") ("rp_user" "first_name" "text")))

(defn 
  db-map
  [lst mp]
  (if (or (nil? lst) (empty? lst))
    mp
    (let [key (keyword (ffirst lst))]
      (if (contains? mp key) 
        (db-map (rest lst) (concat (key mp) (rest (first lst))))
        (db-map (rest lst) (into mp [(keyword (ffirst lst)) (rest (first lst))]))))))

(defn
  create-db-map
  []
  (db-map lst #{}))

