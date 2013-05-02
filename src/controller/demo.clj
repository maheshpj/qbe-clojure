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

(defn index
  ([] (index '()))
  ([req-map]
    (html5
      [:head
       [:meta {:charset "utf-8"}]
       [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
       [:meta {:name "cache" :content "false"}]
       [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
       [:title "AutoQuery Demo"]
       (include-css "/css/demo.css") 
       (include-js "/js/demo.js")
       (include-js "/js/jquery-1.9.1.min.js")
       (include-js "/js/ajaxed.js")]
      [:body {:class "bdy"}
       (idx/schema-form req-map)])))

(defn process-req
  [req]
  (into {} 
        (filter #(not (st/blank? (val %))) 
                (keywordize-keys req))))

(defn run
  [req]
  (if-not (if-nil-or-empty req)
    (html5 
      (idx/result (process-req req)))
    (ring/redirect "/")))

(defn filtersel
  [req toggle]
  (if-not (if-nil-or-empty req)
    (html5 
      (if toggle
        (idx/bullets (conj (process-req req) [:SELECTED "true"]))
        (idx/bullets (process-req req))))
    (ring/redirect "/")))

(defroutes routes
  (GET "/" [] (index))
  (POST "/run" request (run (:form-params request)))
  (POST "/filtersel/on" request (filtersel (:form-params request) true))
  (POST "/filtersel/off" request (filtersel (:form-params request) false)))