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
