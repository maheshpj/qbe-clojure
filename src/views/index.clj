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
     {:style "border: 1px solid grey; width: 80%"}
     [:tr 
      (utils/map-tag 
        :th 
        {:style "text-align: left; color: blue"} 
        (map #(replace-first % "." " ") clm-names-vec))]
     [:tr 
      (for [x data-map]
        [:tr x
         (utils/map-tag 
               :td 
               {:style "text-align: left; color: grey; border-top: 1px solid grey;"} 
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
      {:style "font-weight: bold"} 
      (upper-case 
        (replace-first x "AMS_" ""))
      (for [y (get map x)]
          [:li 
           (let [clmname (create-id "CLM." x y)
                 txtname (create-id "TXT." x y)]
             (check-box {:id clmname :onclick (dislay-cr-txt txtname)} clmname))
           (upper-case (:column_name y))
           [:br]
           (let [txtname (create-id "TXT." x y)]
             (text-field {:placeholder (str "criteria " (:type_name y)) :id txtname :style "display: none"} txtname))])
      [:br]])])

(defn
  create-grid
  [caption clm-names-vec data-map]
  (list
    [:h1 caption]
    (grid clm-names-vec data-map)))


(defn
  create-list
  [caption map]
  (list    
    [:h1 caption]
    [:div {:style "overflow: auto; height: 520px"}
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
    (action/get-result op cr)))

(defn
  schema-form
  [op cr]
  [:div {:id "content"} 
   [:div 
    {:id "schema-form" :style "float: left; width: 25%"} 
    (form-to {:enctype "application/x-www-form-urlencoded"} [:post "/run"]
             (create-schema)
             (submit-button 
               {:style "width: 250px; height: 30px; background-color: lightsteelblue; color: saddlebrown; font: 18px bold;"} 
               "Run!"))]
   [:div 
    {:id "result" :style "float: left; width: 70%; margin-left: 15px"} 
    (create-result-table op cr)]])