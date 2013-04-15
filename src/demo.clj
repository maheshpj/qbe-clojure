(ns ^{:author "Mahesh Jadhav"
      :doc "Compojure app for demo."}
  demo
  (:use [compojure.core :only (defroutes)]
        [ring.adapter.jetty :only (run-jetty)]
        [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.handler :as handler] 
            [controller.demo :as cntr]
            [compojure.route :as route]))

(def host "localhost") ;"172.21.76.154"
(def port 8080)
(def context nil)

(defroutes routes
  cntr/routes
  (route/resources "/")
  (route/files "/" {:root "./resources/public/"})
  (route/not-found "<h1>Oooopsss ... Page not found :( </h1>"))

(def application (handler/site routes))

(defn 
  -main 
  [& args]
  (run-jetty application {:port port :join? false})
  (println "Welcome to the AutoQuery Demo. Browse to" (str "http://" host ":" port) "to get started!"))