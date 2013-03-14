(ns views.index
  (:use [hiccup.page :only (html5)]
        [hiccup.form]
        [hiccup.element]
        [clojure.string :only (upper-case replace-first capitalize)])
  (:require [demo.action :as action]
            [utils]))

(defn
  grid
  [clm-names-vec data-map]
  [:table 
     {:style "border: 1px solid grey; width: 100%"}
     [:thead
     [:tr 
      (utils/map-tag 
        :th 
        {:style "text-align: left; color: dimgray"} 
        (map #(replace-first % "." " ") clm-names-vec))]]
     [:tbody
     [:tr {:style "background: -moz-linear-gradient(top, #ffffff, #dddddd);"}
      (for [x data-map]
        [:tr  x
         (utils/map-tag 
           :td 
           {:style "text-align: left; color: grey; border-top: 0px solid grey; background: -moz-linear-gradient(top, #fdfdfd, #e5e5e5);"} 
           x)])]]])

(defn
  create-id
  [prefix tab name]
  (str prefix "." tab "." (:column_name name)))

(defn
  dislay-cr-txt
  [clmnm name]
  (let [display (str "document.getElementById('" name "').style.display=")
        clm (str "document.getElementById('" clmnm "').checked")]
    (str "if ("clm") {" 
         display "'inline';}"
         "else {"
         display "'none';}")))

(defn
  show-div
  [x y req-map]
  (if ((keyword (create-id "CLM" x y)) req-map) 
    "inline" 
    "none"))

(defn
  option-criteria
  [x y req-map]
  (let [txtname (create-id "TXT" x y)]
    (text-field {:placeholder (str "criteria " (:type_name y)) 
                 :id txtname
                 :style "margin:5px 5px 5px 20px"} 
                txtname
                ((keyword txtname) req-map))))

(defn
  option-ord-by
  [x y req-map]
  (let [ordname (create-id "ORD" x y)]
    (check-box {:id ordname} 
               ordname 
               ((keyword ordname) req-map))))

(defn
  clm-options
  [x y req-map]
  [:div {:id (create-id "DIV" x y)  
         :style (str "display:" (show-div x y req-map))}
   (option-criteria x y req-map)
   (option-ord-by x y req-map) "^"])

(defn
  clm-checkbox
  [x y req-map]
  (let [clmname (create-id "CLM" x y)
        txtname (create-id "TXT" x y)]
    (check-box {:id clmname 
                :onclick (dislay-cr-txt clmname (create-id "DIV" x y))} 
               clmname
               ((keyword clmname) req-map))))

(defn
  tbli-disp
  [id]
  (str "document.getElementById('" id  "').style.display"))

(defn
  li-toggle
  [id]
  (str "if (" (tbli-disp id) " == 'inline') "
       "{" (tbli-disp id) " = 'none';} "
       "else {" (tbli-disp id) " = 'inline';}"))

(defn
  bullets
  "Create left side panel of Table - column tree"
  [req-map map]
  [:ul
   (for [x (keys map)]
     [:li {:style "font-weight: bold; color: dimgray; list-style: square;"
           ;:onclick (li-toggle (str "ul_" x))
           } 
      (upper-case (replace-first x "rp_" ""))
      [:ul {:style "display: inline;" :id (str "ul_" x)}
       (for [y (get map x)]
         [:li {:style "color: #616161; font-weight: normal; list-style: none; padding-left: -50px;"}
          (clm-checkbox x y req-map)
          (upper-case (:column_name y))
          [:br]
          (clm-options x y req-map)])
       [:br]]])])

(defn
  create-grid
  [caption clm-names-vec data-map]
  (list
    [:h2 caption]
    [:div {:style "overflow-y: auto; height:530px; border: 1px solid lightgrey"}
     (grid clm-names-vec data-map)]))


(defn
  create-list
  [req-map caption map]
  (list    
    [:h2 caption]
    [:div {:style "width: 100%; margin-bottom: 4px"} 
     "Root:  "
     (let [options (map 
                     #(upper-case (replace-first  % "rp_" "")) 
                     (keys map))]
       (drop-down {:id "RT"} 
                  "RT" 
                  (cons nil options) 
                  (:RT req-map)))]
    [:div {:style "overflow: auto; height: 500px; border: 1px solid lightgrey; background-color: papayawhip;"}
     (bullets req-map map)]))


(defn 
  create-schema
  [req-map]
  (create-list
    req-map
    "Schema"
    (action/get-schema)))

(defn 
  create-result-table
  [req-map]
  (create-grid 
    "Result"
    (reverse (action/get-header-clms))
    (let [result (action/get-result)]
      (println result)
      result)))

(defn
  create-run-btn
  []
  (submit-button 
    {:style "float: right; width: 150px; border: 1px solid white; height: 30px; background-color: darkkhaki; margin-top: 5px; color: saddlebrown; font: 18px bold;"} 
    "Run!"))

(defn
  create-reset-btn
  []
  [:div {:style "float: left; background-color: lightgrey; margin-top: 7px; width: 55px; text-align: center; height: 25px; vertical-align: middle; line-height: 20pt; border: 1px solid grey"}
   (link-to "/" "Reset")])

(defn
  schema-form
  [req-map]
  [:div {:id "content"} 
   [:div 
    {:id "schema-form" :style "float: left; width: 25%"} 
    (form-to 
      {:enctype "application/x-www-form-urlencoded"} [:post "/run"]
      (create-schema req-map) 
      (create-reset-btn)
      (create-run-btn))]
   ;(println req-map)
   (when-not (utils/if-nil-or-empty req-map)
     (action/create-query-seqs req-map)
     [:div 
      {:id "result" :style "float: left; width: 70%; margin-left: 15px"} 
      (if (utils/if-nil-or-empty action/rt)
        (label {:style "color: red; font-size: 13pt"} "errMsg" "Please select a Root")
        (create-result-table req-map))])])