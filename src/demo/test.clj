(ns demo.test)


(def k '(3 (1 g) (4 b)))


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
