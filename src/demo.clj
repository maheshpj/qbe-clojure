(ns ^{:author "Mahesh Jadhav"
      :doc "Compojure app for demo."}
  demo
  (:use [compojure.core :only (defroutes)]
        [ring.adapter.jetty :only (run-jetty)])
  (:require [controller.demo :as cntr]
            [compojure.route :as route]))

(def host "localhost")
(def port 8080)
(def context nil)

(defroutes app
  cntr/routes
  (route/files "/")
  (route/not-found "<h1>Oooopsss ... Page not found :( </h1>"))

(defn 
  -main 
  [& args]
  (run-jetty (var app) {:port port :join? false})
  (println "Welcome to the Demo. Browse to" (str "http://" host ":" port) "to get started!"))

