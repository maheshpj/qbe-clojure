(ns utils
  (:require [clojure.string :only (trim upper-case replace blank?) :as st]))

(def prf "AMS_")
(def CLM "CLM")
(def TXT "TXT")
(def ORD "ORD")
(def DIV "DIV")
(def HDN "HDN")
(def RT "RT")

(def proj_selected_tables ["ams_asset" "ams_program" "ams_wf_state_smy" "ams_account"])
(def db-grph-keys [:fkcolumn_name :pktable_name :fktable_name])

(defn
  convert-form-string-to-map
  "converting request string '1=1&2=2&3=3' into map {:1 1, :2 2, :3 3}"
  [form-str]
  (reduce 
    #(assoc % (keyword (read-string (nth %2 1))) (nth %2 2)) {} 
      #_> (re-seq #"([^=&]+)=([^=&]+)" form-str)))


(defn map-tag [tag style-map xs]
  (map (fn [x] [tag style-map x]) xs))

(defn
  if-nil-or-empty
  [any]
  (or 
    (nil? any) 
    (empty? any)))


(defn
  val-up
  [vl]
  (st/upper-case (st/trim vl)))