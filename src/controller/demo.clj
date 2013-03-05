(ns controller.demo
  (:use [compojure.core :only (defroutes GET POST)]
        [ring.adapter.jetty :only (run-jetty)]
        [hiccup.page :only (html5)])
  (:require [compojure.route :as route]
            [views.index :as idx]
            [utils]
            [ring.util.response :as ring]))

(defn 
  index 
  [lst]
  (html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
     [:title "AutoQuery Demo"]]
    [:body (idx/schema-form lst)]))

(defn
  run
  [req]
  (when-not (utils/if-nil-or-empty req)
    (index (reverse (map 
              name 
              (keys (utils/convert-form-string-to-map
                      (slurp (req :body)))))))))

(defroutes 
  routes
  (GET "/" [] (index '()))
  (POST "/run" request (run request)))