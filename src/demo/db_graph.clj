(ns demo.db-graph
  (:use [loom.graph])
  (:require [loom.alg :as ag]
            [demo.action :as action]))

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

(def g (graph wdg))

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
  (ag/shortest-path g (keyword action/rt) (keyword end)))

(defn
  rem-root-from-sel-tables
  []
  (remove #(= action/rt %) action/sel-tables))

(defn
  get-distinct-nodes
  "Get distinct nodes after finding the path with each selected table and Root table"
  []
  (apply hash-set 
         (apply merge 
                (map (fn [end] (root-short-path end)) 
                     (rem-root-from-sel-tables)))))
