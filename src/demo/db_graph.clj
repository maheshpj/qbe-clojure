(ns demo.db-graph
  (:use [loom.graph]
        [loom.alg])
  (:require [demo.action :as axn]))

(def owdg (weighted-digraph 
               [:paa :ast "asset_id"] 
               [:paa :prg "prog_id"] 
               [:st :ast "asset_id"] 
               [:prg :act "act_id"]
               [:ph :prg "sub_id"] 
               [:ph :prg "rel_id"] 
               [:itm :ast "asset_id"]))

(def wdg (weighted-digraph 
               [:rp_authors :rp_user "user_id"]))

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
  [end]
  (shortest-path g (keyword axn/rt) (keyword end)))

(defn
  rem-root-from-sel-tables
  []
  (remove (fn [tb] (= axn/rt tb)) axn/sel-tables))

(defn
  filter-keys
  [distinct-nodes]
  (select-keys (bf-span g (keyword axn/rt)) 
               distinct-nodes))

(defn
  filter-nodes
  [col distinct-nodes]
  (reverse (filter (fn [tb] (contains? distinct-nodes tb)) col)))

(defn
  get-distinct-nodes
  "Get distinct nodes/set after finding the path with each selected table and Root table"
  []
  (set (reduce (fn [ls1 ls2] (concat ls1 ls2)) 
               (map (fn [end] (root-short-path end)) 
                    (rem-root-from-sel-tables)))))

(defn 
  get-join-tree
  "Get a Map of spanning tree which includes all 'join' nodes"
  []
  (let [distinct-nodes (get-distinct-nodes)
        treemap (filter-keys distinct-nodes)]
    (into {}
          (reverse
            (zipmap
              (keys treemap)
              (map (fn [node] (filter-nodes node distinct-nodes)) 
                   (vals treemap)))))))

(defn
  get-edge
  "get list of fk relation like ((:fk-table :pk-table) 'clm-name' "
  [g n1 n2]
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
       (demo.db/table-pk (second (first fk-edge)))))

(defn
  create-join
  []
  (let [jointr (get-join-tree)]
    (str axn/rt demo.db/blank (create-on-joins))))



