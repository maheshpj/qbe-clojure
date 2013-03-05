(ns ^{:author "Mahesh Jadhav"
      :doc "Compojure app for demo."}
  demo
  (:use [compojure.core :only (defroutes GET POST)]
        [ring.adapter.jetty :only (run-jetty)]
        [hiccup.page :only (html5)]
        [hiccup.form]
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
    (form-to {:enctype "application/x-www-form-urlencoded"} [:post "/"]
             (submit-button "Run!")
             (let [tbmap (content2)]
               (for [x (keys tbmap)]
                 [:ul (check-box (str x) false false) x])))))

(defn index []
  (html5
    [:head
     [:title "AutoQuery Demo"]]
    [:body
     [:div {:id "content"} (create-DBtable-list)]]))

(defn
  convert-form-string-to-map
  [form-str]
  (reduce 
    #(assoc % (keyword (read-string (nth %2 1))) (nth %2 2)) {} 
      #_> (re-seq #"([^=&]+)=([^=&]+)" form-str)))

(defn 
  post-req
  [req]
  (when-not (= nil req)
    (println 
      (convert-form-string-to-map
        (slurp (req :body)))))
  (index))


(defroutes app
  (GET "/" [] (index))
  (POST "/" request (post-req request))
  (route/not-found "<h1>Oooopsss ... Page not found :( </h1>"))

(defn 
  -main 
  [& args]
  (run-jetty (var app) {:port port :join? false})
  (println "Welcome to the Demo. Browse to" (str "http://" host ":" port) "to get started!"))

