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
     {:style "border: 1px solid grey; width: 60%"}
     [:tr 
      (utils/map-tag 
        :th 
        {:style "text-align: left; color: blue"} 
        clm-names-vec)]
     [:tr 
      (for [x data-map]
        [:tr 
         (utils/map-tag 
               :td 
               {:style "text-align: left; color: grey; border-top: 1px solid grey;"} 
               x)])]])

(defn
  bullets
  [map]
  [:ul
   (for [x (keys map)]
     [:li 
      {:style "font-weight: bold"} 
      (upper-case 
        (replace-first x "rp_" ""))
      (for [y (get map x)]
        [:li 
         (check-box 
           {:id (str "cb_" (:column_name y))} 
           (str x "." (:column_name y))
           false)
         (upper-case (:column_name y))])
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
    (bullets map)))


(defn 
  create-schema
  []
  (create-list
    "Schema"
    (action/get-schema)))

(defn 
  create-result-table
  [lst]
  (create-grid 
    "Result"
    (action/get-header-clms lst)
    (action/get-result lst)))

(defn
  schema-form
  [lst]
  [:div {:id "content"} 
   [:div 
    {:id "schema-form" :style "float: left; width: 25%"} 
    (form-to {:enctype "application/x-www-form-urlencoded"} [:post "/run"]
             (create-schema)
             ;(text-field {:size 3} :x) 
             ;(text-area {:placeholder "shout"} "shout")
             (submit-button "Run!"))]
   [:div 
    {:id "result" :style "float: left; width: 75%"} 
    (create-result-table lst)]])