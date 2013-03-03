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

(defn index []
  (html5
    [:head
     [:title "AutoQuery Demo"]]
    [:body
     [:div {:id "content"} (content)]]))

(defroutes app
  (GET "/" [] (index))
  (route/not-found "<h1>Oooopsss ... Page not found :( </h1>"))

(defn 
  -main 
  [& args]
  (run-jetty (var app) {:port port :join? false})
  (println "Welcome to the Demo. Browse to http://" host ":" port "to get started!"))

