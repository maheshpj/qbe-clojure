(ns demo.db-graph
  (:use [loom.graph]
        [loom.alg]
        [utils])
  (:require [clojure.string :only (join) :as st]))

(def owdg)
(def g)
(def sel-tables)
(def table-pk)

(defn selected-tables
  [col]
  (into #{} (map (fn [i] (first (st/split i #"\."))) col)))

(defn root-short-path2
  [root end]
  (let [asoc-tbl (keys (filter (fn [i] (= (val i) (hash-set root end))) (:adj g)))]
    (if (if-nil-or-empty asoc-tbl)
      (shortest-path g (keyword root) (keyword end))
      asoc-tbl)))

(defn root-short-path
  [root end]
  (shortest-path g (keyword root) (keyword end)))

(defn rem-root-from-sel-tables
  [root]
  (remove (fn [tb] (= (st/upper-case root) (st/upper-case tb))) sel-tables))


(defn check-kee
  [kee distinct-nodes]
  (some (fn [node] (= node kee)) distinct-nodes))

(defn filter-keys
  [root distinct-nodes spantrkeys]
  (filter (fn [kee] (check-kee kee distinct-nodes)) spantrkeys))

(defn filter-nodes
  [col distinct-nodes]
  (filter (fn [tb] (contains? distinct-nodes tb)) col))

(defn get-distinct-nodes
  "Get distinct nodes/set after finding the path with each selected table and Root table"
  [root]
  (set (reduce (fn [ls1 ls2] (concat ls1 ls2)) 
               (map (fn [end] (root-short-path root end)) 
                    (rem-root-from-sel-tables root)))))

(defn joins
  "Get a Map of spanning tree which includes all 'join' nodes"
  [root]
  (let [distinct-nodes (get-distinct-nodes root)
        sub-graph (subgraph g distinct-nodes)]
    (into {} 
          (reverse (select-keys (bf-span sub-graph root)
                                (bf-traverse sub-graph root))))))


;;;; may need to change
(defn get-edge
  "get list of fk relation like ((:fk-table :pk-table) 'clm-name') "
  [g n1 n2]
  (if (has-edge? g n1 n2)
    (cons (bf-path g n1 n2) (list (weight g n1 n2)))
    (cons (bf-path g n2 n1) (list (weight g n2 n1)))))

(defn create-on-joins
  "create ON--JOIN relation e.g 'tab1.pk = tab2.fk', input is output of get-edge()"
  [fk-edge]
  (str (name (ffirst fk-edge)) "." 
       (second fk-edge) " = " 
       (name (second (first fk-edge))) "." 
       (table-pk (second (first fk-edge)))))

(defn left-otr-join
  [i rt-bool flst]
  (str " LEFT OUTER JOIN "  (name i) " ON " 
       (create-on-joins 
         (if rt-bool (get-edge owdg i flst)
           (get-edge owdg flst i)))))

(defn create-onjoins
  [lst rt-bool]
  (st/join (map (fn [i] (left-otr-join i rt-bool (first lst))) (second lst))))

(defn process-root-join
  [root-join]
  (create-onjoins root-join true))

(defn process-rest-join
  [mp]
  (st/join (map #(create-onjoins % false) mp)))

(defn create-join
  [db-grph root op tbpk]  
  (def owdg (apply weighted-digraph db-grph))
  (def g (graph owdg))
  (def sel-tables (selected-tables op))
  (def table-pk tbpk) 
  (let [join-tree (joins root)]
    (str (process-root-join (reverse (into () (first join-tree))))    
         (process-rest-join (into {} (rest join-tree))))))