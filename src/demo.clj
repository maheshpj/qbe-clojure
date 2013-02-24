(ns ^{:author "Mahesh Jadhav"
      :doc "Compojure app for demo."}
  demo
  (:use [compojure.core :only (defroutes GET)]
        [ring.adapter.jetty :only (run-jetty)])
  (:require [compojure.route :as route]))

(defroutes app
  (GET "/" [] "<h1>Hello World</h1>")
  (route/not-found "<h1>Page not found</h1>"))

(defn 
  -main 
  [& args]
  (run-jetty (var app) {:port 8080 :join? false})
  (println "Welcome to the Demo. Browse to http://localhost:8080 to get started!"))

