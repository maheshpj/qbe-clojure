(ns views.index
  (:use [hiccup.page :only (html5)]
        [hiccup.form]
        [clojure.string :only (upper-case replace-first capitalize)])
  (:require [demo.action :as action]
            [utils]))

(defn
  grid
  [clm-names-vec data-map]
  [:table 
     {:style "border: 1px solid grey; width: 100%"}
     [:tr 
      (utils/map-tag 
        :th 
        {:style "text-align: left; color: dimgray"} 
        (map #(replace-first % "." " ") clm-names-vec))]
     [:tr {:style "background: -moz-linear-gradient(top, #ffffff, #dddddd);"}
      (for [x data-map]
        [:tr x
         (utils/map-tag 
               :td 
               {:style "text-align: left; color: grey; border-top: 0px solid grey; background: -moz-linear-gradient(top, #fdfdfd, #e5e5e5);"} 
               x)])]])

(defn
  create-id
  [prefix tab name]
  (str prefix tab "." (:column_name name)))

(defn
  dislay-cr-txt
  [name]
  (let [display (str "document.getElementById('" name "').style.display=")]
    (str "if (this.checked) {" 
         display "'inline';}"
         "else {"
         display "'none';}")))

(defn
  bullets
  [map]
  [:ul
   (for [x (keys map)]
     [:li 
      {:style "font-weight: bold; color: dimgray"} 
      (upper-case 
        (replace-first x "AMS_" ""))
      (for [y (get map x)]
          [:li {:style "color: #616161; font-family: Century Gothic;"}
           (let [clmname (create-id "CLM." x y)
                 txtname (create-id "TXT." x y)]
             (check-box {:id clmname :onclick (dislay-cr-txt txtname)} clmname))
           (upper-case (:column_name y))
           [:br]
           (let [txtname (create-id "TXT." x y)]
             (text-field {:placeholder (str "criteria " (:type_name y)) 
                          :id txtname 
                          :style "display: none"} 
                         txtname))])
      [:br]])])

(defn
  create-grid
  [caption clm-names-vec data-map]
  (list
    [:h1 caption]
    [:div {:style "overflow-y: auto; height:530px; border: 1px solid lightgrey"}
     (grid clm-names-vec data-map)]))


(defn
  create-list
  [caption map]
  (list    
    [:h1 caption]
    [:div {:style "overflow: auto; height: 530px; border: 1px solid lightgrey; background-color: papayawhip;"}
     (bullets map)]))


(defn 
  create-schema
  []
  (create-list
    "Schema"
    (action/get-schema)))

(defn 
  create-result-table
  [op cr]
  (create-grid 
    "Result"
    (action/get-header-clms op)
    (let [result (action/get-result op cr)]
      (println result)
      result)))

(defn
  create-run-btn
  []
  (submit-button 
    {:style "float: right; width: 284px; height: 30px; background-color: darkkhaki; margin-top: 5px; color: saddlebrown; font: 18px bold;"} 
    "Run!"))

(defn
  schema-form
  [op cr]
  [:div {:id "content"} 
   [:div 
    {:id "schema-form" :style "float: left; width: 25%"} 
    (form-to 
      {:enctype "application/x-www-form-urlencoded"} [:post "/run"]
      (create-schema)             
      (create-run-btn))]
   [:div 
    {:id "result" :style "float: left; width: 70%; margin-left: 15px"} 
    (create-result-table op cr)]])