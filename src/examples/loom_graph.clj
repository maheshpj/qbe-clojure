(ns examples.loom-graph
  (:use [loom.graph])
  (:require [loom.alg :as ago]))

(def wdg (weighted-digraph [:a :b 10] [:a :c 20] [:c :d 30] [:d :b 10]))

(def ams-wdg (weighted-digraph 
               [:paa :ast "asset_id"] 
               [:paa :prg "prog_id"] 
               [:st :ast "asset_id"] 
               [:prg :act "act_id"]
               [:ph :prg "sub_id"] 
               [:ph :prg "rel_id"] 
               [:itm :ast "asset_id"]))

(def g (graph ams-wdg))



