(ns ^{:author "Mahesh Jadhav"
      :doc "Compojure app for demo."}
  demo
  (:use [compojure.core :only (defroutes GET)]
        [ring.adapter.jetty :only (run-jetty)]
        [hiccup.page :only (html5)]
        [demo.action])
  (:require [compojure.route :as route]))

(def host "localhost")
(def port 8080)

(defn map-tag [tag xs]
  (map (fn [x] [tag x]) xs))

(defn 
  create-result 
  []
  (list
   [:h1 "Results"]
   [:hr]
   [:table {:style "border: 1px solid grey; width: 90%"}
    [:tr 
      (map-tag :th ["Project Id" "Account" "Program" "Project" "Asset ID" "Asset Type" "ATH"])]
    [:tr 
     (for [x (content)]
       [:tr {:style "border: 1px solid grey;"} (map-tag :td x)])]]))

(defn 
  create-DBtable-list 
  []
  (list
    [:h1 "Tables"]
    [:hr]
    [:ul 
     (for [[tbl clmns] (content2)]
       [:ul (map-tag :ul tbl)])]))

(defn index []
  (html5
    [:head
     [:title "AutoQuery Demo"]]
    [:body
     [:div {:id "content"} (create-DBtable-list)]]))

(defroutes app
  (GET "/" [] (index))
  (route/not-found "<h1>Oooopsss ... Page not found :( </h1>"))

(defn 
  -main 
  [& args]
  (run-jetty (var app) {:port port :join? false})
  (println "Welcome to the Demo. Browse to" (str "http://" host ":" port) "to get started!"))

