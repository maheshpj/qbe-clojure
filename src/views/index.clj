(ns views.index
  (:use [hiccup.page :only (html5)]
        [hiccup.form]
        [hiccup.element]
        [clojure.string :only (upper-case replace-first capitalize)]
        [utils]
        [demo.db-config])
  (:require [demo.action :as action]))

(def root-err "Please select a 'Report for' value")
(def form-enctype "application/x-www-form-urlencoded")
(def beginning-time)

(defn header-name
  [vect]
  (map #(replace-first (capitalize (name %)) "." " ") vect))

(defn cell-style
  [x]
  (str "color: " (if (map? x) "grey" "red") "; border-right: 1px solid silver;"))

(defn grid
  [clm-names-vec data-map]
  [:table 
   [:thead
    [:tr (map-tag :th {:style "border-right: 1px solid silver;"} (header-name (keys (first data-map))))]] 
   [:tbody
    (for [x data-map] [:tr (map-tag :td {:style (cell-style x)} x)])]])

(defn create-id
  [prefix tab name]
  (str prefix "." tab (if (nil? name) "" (str "." (:column_name name)))))

(defn show-div
  [x y req-map]
  (if ((keyword (create-id CLM x y)) req-map) "table-row" "none")) 

(defn option-criteria
  [x y req-map]
  (let [txtname (create-id TXT x y)]
    (text-field {:placeholder (str "criteria " (:type_name y)) 
                 :id txtname
                 :class "crit-txt"} 
                txtname ((keyword txtname) req-map))))

(defn option-grp
  [x y req-map]
  (let [grpname (create-id GRP x y)]
    [:div
     (drop-down {:id grpname :class "drp-down"} 
                grpname (cons nil group-fun) 
                ((keyword grpname) req-map))
     (label {:class "drp-dwn-lbl" :style "color: white"} "Group" "Group") ]))

(defn gen-cb-option
  [x y req-map prfx nm]  
  (let [name (create-id prfx x y)]
      [:span
       (check-box {:id name :class "drp-down"} name ((keyword name) req-map))
       (label {:class "drp-dwn-lbl" :style "color: white"} nm nm)]))

(defn clm-options
  [x y req-map]
  [:div {:id (create-id DIV x y)  
         :style (str "display:" (show-div x y req-map))} 
   [:div#opt-div {:style "border: 1px solid lightgrey; background-color: cadetblue; text-color: white"}
    (option-criteria x y req-map)
    (option-grp x y req-map)  
    (gen-cb-option x y req-map MTA "Code to Name")
    (gen-cb-option x y req-map EXC "Exclude")
    (gen-cb-option x y req-map ORD "Sort")]])

(defn clm-checkbox
  [x y req-map]
  (let [clmname (create-id CLM x y)
        divid (create-id DIV x y)]
    (check-box {:id clmname 
                :onclick (str "dislayOptions('" clmname "', '"divid"')")}
               clmname ((keyword clmname) req-map))))

(defn tr-class
  [x req-map]
  (let [val ((keyword (create-id HDN x nil)) req-map)]
    (if (nil? val) "closed" val)))

(defn isselect
  [sel]
  (if (= "true" sel) true false))

(defn show-selected
  [showselected val]
  (if (= "true" showselected) (isselect val) true))

(defn get-branch
  [req-map mp x]
  (for [y (sort-by :column_name (get mp x))]
    (when-not (some #(= (upper-case (:column_name y)) %) rem-clms)
      (if (show-selected (:SELECTED req-map) 
                         ((keyword (create-id CLM x y)) req-map))
        [:li (clm-checkbox x y req-map)
         (upper-case (:column_name y)) [:br]
         (clm-options x y req-map)]))))

(defn hdn-field
  [x req-map]
  (let [hdnf (create-id HDN x nil)]
    (hidden-field  {:id hdnf} hdnf (tr-class x req-map))))

(defn tblname-no-prf
  [i]
  (if (if-nil-or-empty prf)
    (upper-case i)
    (upper-case (replace-first i prf ""))))

(defn li-table
  [req-map map x]
  [:li (link-to {:style "text-decoration: none;" :id (str "img_" x) 
                 :border "0" :onclick (str "toggle('" x "');")} "#" "+ ")
   (tblname-no-prf x)
   (hdn-field x req-map)
   [:ul {:class (tr-class x req-map) :id (str "ul_" x)}
    (get-branch req-map map x)
    [:br]]])

(defn bullets
  "Create left side panel of Table - column tree"
  [req-map map]
  (let [seld (:SELECTED req-map)
        kees (keys req-map)]
    [:ul {:class "open"}
     (for [x (sort (keys map))]
       (if (nil? seld)
         (li-table req-map map x)
         (when (some (fn [i] (.startsWith (name i) (str CLM "." x "."))) kees)
           (li-table req-map map x))))]))

(defn create-records-lable
  [cnt]
  (label {:style "float:right;"} nil (str "No of records:" " " cnt)))

(defn create-time-lable
  []
  (label {:style "float:left; color: silver"} nil 
         (str "Time taken:" " " (timeTaken beginning-time) " mins")))

(defn create-grid
  [caption clm-names-vec data-map]
  (list 
    [:div {:class "grid-div"} (grid clm-names-vec data-map)]
    (create-records-lable (count data-map))
    (create-time-lable)))

(defn get-options
  [mp]
  (map #(tblname-no-prf %) (keys mp)))

(defn create-list
  [req-map caption map]
  (list    
    [:div#root-div "Report for:  "
     (drop-down {:id RT} RT 
                (cons nil (sort (get-options map))) 
                (:RT req-map)) [:font {:class "required"} "    *"]]
    [:div#list-div (bullets req-map map)]
    [:div
     (text-area {:id EXT :placeholder "Extra Column" :style "width:99%"} EXT (:EXT req-map))]
    [:div  (check-box "SELECTED" (:SELECTED req-map)) " Show Selected Only" ]))


(defn create-schema
  [req-map]
  (create-list req-map "Schema" (action/get-schema)))

(defn create-result-table
  [req-map]
  (def beginning-time (java.util.Date.))
  (let [res (action/get-result)
        err (:Error res)]
    (if err
      (label {:class "err-msg"} "err" err)
      (create-grid "Result" (reverse (action/get-header-clms)) res))))

(defn create-run-btn
  []
  (submit-button {:class "run"} "Run!"))

(defn create-reset-btn
  []
  [:div#reset (link-to "/" "Reset")])

(defn result
  [req-map]
  (when-not (if-nil-or-empty req-map)
    (action/create-query-seqs req-map)
    [:div {:id "result" :class "res-div"} 
     (if (if-nil-or-empty action/rt)
       (label {:class "err-msg"} "errMsg" root-err)
       (create-result-table req-map))]))

(defn schema-form
  [req-map]
  [:div {:id "content"} 
   [:div {:id "schema-form" :class "schema-div"} 
    (form-to 
      {:enctype form-enctype} [:post "/run"]
      (create-schema req-map) 
      (create-reset-btn)
      (create-run-btn))]
   (result req-map)])