(ns controller.demo
  (:use [compojure.core :only (defroutes GET POST PUT)]
        [ring.adapter.jetty :only (run-jetty)]
        [hiccup.page :only (html5 include-css)])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.util.response :as ring]
            [views.index :as idx]
            [utils]))

(defn 
  index
  ([] (index '()))
  ([req-map]
    ;(println req-map)
    (html5
      [:head
       [:meta {:charset "utf-8"}]
       [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
       [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
       [:title "AutoQuery Demo"]]
      [:body {:style "font-family: Century Gothic; background-color: oldlace;"} 
       (idx/schema-form req-map)])))

(defn
  run
  [req]
  ;(println req)
  (if-not (utils/if-nil-or-empty req)
    (index (utils/convert-form-string-to-map
             (slurp (req :body))))
    (ring/redirect "/")))

(defroutes 
  routes
  (GET "/" [] (index))
  (POST "/run" request (run request)))