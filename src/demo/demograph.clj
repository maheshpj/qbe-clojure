(ns demo.demograph)

; define table directed graph
(def 
  tablegraph 
  '((2 1) (7 1) (3 1 4) (4 5) (6 4))) ;{node neighbors) (2 1)

(defn
  our-assoc
  [node net]
  (if (or (nil? net) (empty? net)) 
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
  (if (or (nil? queue) (empty? queue)) 
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
  "Breadth first search algorithm"
  [start end net]
  (bfs end (list (list start)) net))
  