(ns examples.calculator
  (:use compojure)) 

(defn html-doc 
  [title & body] 
  (html 
    (doctype :html4) 
    [:html 
      [:head 
        [:title title]] 
      [:body 
       [:div 
        [:h2 
         ;; Pass a map as the first argument to be set as attributes of the element
         [:a {:href "/"} "Home"]]]
        body]])) 


(def sum-form 
  (html-doc "Sum" 
    (form-to [:post "/"] 
      (text-field {:size 3} :x) 
      "+" 
      (text-field {:size 3} :y) 
      (submit-button "=")))) 

(defn result 
  [x y] 
  (let [x (Integer/parseInt x) 
        y (Integer/parseInt y)] 
    (html-doc "Result" 
      x " + " y " = " (+ x y)))) 

(defroutes webservice
  (GET "/" 
    sum-form) 
  (POST "/" 
    (result (params :x) (params :y)))) 

(run-server {:port 8080} 
  "/*" (servlet webservice))