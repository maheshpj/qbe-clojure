(ns demo.demograph)

; define table weighted directed graph
(def 
  wdg
  '((2 (1 a)) (3 (1 g) (4 b)) (4 (5 c)) (6 (4 d e)) (7 (1 f))))

; define table directed graph
(def 
  dg 
  '((2 1) (3 1 4) (4 5) (6 4) (7 1))) ;{node neighbors) (2 1)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn
  if-nil-or-empty
  [any]
  (or 
    (nil? any) 
    (empty? any)))
  
(defn
  getrest2 
  [restlst simplerestlst]
  (if (if-nil-or-empty restlst)
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
  (cons 
    (first list) 
    (getrest (rest list))))

(defn
  dg-to-wdg
  [wdg dg]
  (if (if-nil-or-empty wdg)
    dg
    (dg-to-wdg 
      (rest wdg)
      (cons 
        (get-simple-list (first wdg))
        dg))))

(defn
  get-dg-from-wdg
  "get directed graph from weighted directed graph"
  [wdg]
  (reverse (dg-to-wdg wdg nil)))

(defn
  our-assoc
  [node net]
  (if (if-nil-or-empty net) 
    nil
    (let [pair (first net)]
      (if (== node (first pair))
        pair
        (our-assoc node (rest net))))))

(defn
  new-paths
  [path node net]
  (println "=> node: " node) 
  (map #(cons % path)
       (rest (our-assoc node net))))

(defn 
  bfs
  [end queue net]
  (println "=> Q: " queue)
  (if (if-nil-or-empty queue) 
    nil
    (let [path (first queue)]
      (println "=> path: " path)
      (let [node (first path)]
        (if (= node end)
          (reverse path)
          (bfs end 
               (concat 
                 (rest queue) 
                 (new-paths path node net))
               net))))))

(defn 
  shortest-path  
  "Breadth first search algorithm on dg"
  [start end net]
  (bfs end (list (list start)) net))
  