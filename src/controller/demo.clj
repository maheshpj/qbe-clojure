(ns controller.demo
  (:use [compojure.core :only (defroutes GET POST PUT)]
        [hiccup.page :only (html5 include-css include-js)]
        [hiccup.util :only (escape-html)]
        [utils]
        [clojure.walk])
  (:require [compojure.route :as route]
            [ring.util.response :as ring]
            [views.index :as idx]
            [clojure.string :only (blank?) :as st]))

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
       [:title "AutoQuery Demo"]
       (include-css "/css/demo.css")
       (include-js "/js/demo.js")]
      [:body {:class "bdy"}
       (idx/schema-form req-map)])))

(defn
  run
  [req]
  (println req)
  (if-not (if-nil-or-empty req)
    (index  (into {} (filter #(not (st/blank? (val %))) (keywordize-keys req))))
    (ring/redirect "/")))

(defroutes 
  routes
  (GET "/" [] (index))
  (POST "/run" request (run (:form-params request))))