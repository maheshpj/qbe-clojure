(ns ^{:author "Mahesh Jadhav"
      :doc "Compojure app for demo."}
  demo
  (:use [compojure.core :only (defroutes)]
        [ring.adapter.jetty :only (run-jetty)])
  (:require [controller.demo :as cntr]
            [compojure.route :as route]))

(def host "localhost") ;"172.21.76.154"
(def port 8080)
(def context nil)

(defroutes routes
  cntr/routes
  (route/files "/")
  (route/not-found "<h1>Oooopsss ... Page not found :( </h1>"))

(def application routes)

(defn 
  -main 
  [& args]
  (run-jetty application {:port port :join? false})
  (println "Welcome to the Demo. Browse to" (str "http://" host ":" port) "to get started!"))

