(ns demo.db-graph
  (:use [loom.graph]
        [loom.alg])
  (:require [clojure.string :only (join) :as st]))

(def owdg (weighted-digraph 
               [:AMS_PGM_ASSET_ALIGNMENT :AMS_ASSET "ASSET_ID"] 
               [:AMS_PGM_ASSET_ALIGNMENT :AMS_PROGRAM "PROGRAM_REF_ID"] 
               [:AMS_PROGRAM :AMS_ASSET "ASSET_ID"] 
               [:AMS_PROGRAM :AMS_ACCOUNT "ACCOUNT_ID"]
               [:AMS_PGM_HCHY :AMS_PROGRAM "SUBJECT_ID"] 
               [:AMS_PGM_HCHY :AMS_PROGRAM "RELATION_ID"] 
               [:AMS_ASSESSMENT_ITEM :AMS_ASSET "ASSET_ID"]))

(def wdg (weighted-digraph 
               [:rp_authors :rp_user "user_id"]))


(def sel-tables)
(def table-pk)
;(def sel-tables #{"ast" "st" "prg" "act"})

(defn 
  selected-tables
  [col]
  (into #{} 
        (map (fn [i] (first (st/split i #"\."))) col)))

(def g (graph owdg))

(defn
  create-WDG
  []
  ())

(defn 
  create-graph
  []
  ())

(defn
  root-short-path
  [root end]
  (shortest-path g (keyword root) (keyword end)))

(defn
  rem-root-from-sel-tables
  [root]
  (remove (fn [tb] (= root tb)) sel-tables))

(defn
  filter-keys
  [root distinct-nodes]
  (select-keys (bf-span g (keyword root)) 
               distinct-nodes))

(defn
  filter-nodes
  [col distinct-nodes]
  (reverse (filter (fn [tb] (contains? distinct-nodes tb)) col)))

(defn
  get-distinct-nodes
  "Get distinct nodes/set after finding the path with each selected table and Root table"
  [root]
  (set (reduce (fn [ls1 ls2] (concat ls1 ls2)) 
               (map (fn [end] (root-short-path root end)) 
                    (rem-root-from-sel-tables root)))))

(defn 
  get-join-tree
  "Get a Map of spanning tree which includes all 'join' nodes"
  [root]
  (let [distinct-nodes (get-distinct-nodes root)
        treemap (filter-keys root distinct-nodes)]
    (into {}
          (reverse
            (zipmap
              (keys treemap)
              (map (fn [node] (filter-nodes node distinct-nodes)) 
                   (vals treemap)))))))

;;;; may need to change
(defn
  get-edge
  "get list of fk relation like ((:fk-table :pk-table) 'clm-name') "
  [g n1 n2]
  (println "n1: " n1 " n2: " n2)
  (if (has-edge? g n1 n2)
    (cons (bf-path g n1 n2) (list (weight g n1 n2)))
    (cons (bf-path g n2 n1) (list (weight g n2 n1)))))

(defn
  create-on-joins
  "create ON--JOIN relation e.g 'tab1.pk = tab2.fk', input is output of get-edge()"
  [fk-edge]
  (str (name (ffirst fk-edge)) 
       "." 
       (second fk-edge) 
       " = " 
       (name (second (first fk-edge))) 
       "." 
       (table-pk (second (first fk-edge)))))

(defn
  create-onjoins
  [lst rt-bool]
  (st/join (map #(str " LEFT OUTER JOIN "  (name %) " ON " 
                      (create-on-joins 
                        (if rt-bool (get-edge owdg % (first lst))
                          (get-edge owdg (first lst) %))))
                (second lst))))

(defn
  process-root-join
  [root-join]
  (str " " (create-onjoins root-join true)))

(defn 
  process-rest-join
  [mp]
  (st/join (map #(create-onjoins % false) mp)))

(defn
  create-join
  [root op tbpk]  
  (def sel-tables (selected-tables op))
  (println "sel-tables are " sel-tables)
  (def table-pk tbpk)
  (println "table-pk are " table-pk)
  (let [join-tree (get-join-tree root)]
    (str
    (process-root-join (reverse (into () (first join-tree))))    
    (process-rest-join (into {} (rest join-tree))))))


