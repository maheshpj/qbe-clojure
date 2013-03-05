(ns demo.geeks
  (:use [compojure core]
        [compojure response]
        [ring.adapter jetty]
        [hiccup.core]
        [hiccup.page]))

(def *geeks*
     [{:name "Pablo"  :nick "pablete"
       :tweet-home "http://twitter.com/pablete"}
      {:name "Mauro"  :nick "malditogeek"
       :tweet-home "http://twitter.com/malditogeek"}
      {:name "Javier" :nick "jneira"
       :tweet-home "http://twitter.com/jneira"}
      {:name "Joel"   :nick "joeerl"
       :tweet-home "http://twitter.com/joeerl"}])

(defn geeks-template
  ([geeks]
     (xhtml-tag :en
       [:head
        [:title "rdfa test"]]
       [:body
        [:h1 "Some geeks"]
        [:div {:id "geeks"}
         (map (fn [geek]
                [:div {:class "geek" :id (:nick geek)}
                 [:p {:class "name"} (:name geek)]
                 [:p {:class "nick"} (:nick geek)]
                 [:a {:class "tweet-home"
                      :href (:tweet-home geek)}
                  (:tweet-home geek)]])
                    geeks)]])))

(defroutes rdfa-test
  (GET "/geeks" request
       (html (geeks-template *geeks*))))

(run-jetty (var rdfa-test) {:port 8081})