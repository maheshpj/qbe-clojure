(ns utils)

(defn
  convert-form-string-to-map
  "converting request string '1=1&2=2&3=3' into map {:1 1, :2 2, :3 3}"
  [form-str]
  (reduce 
    #(assoc % (keyword (read-string (nth %2 1))) (nth %2 2)) {} 
      #_> (re-seq #"([^=&]+)=([^=&]+)" form-str)))


(defn map-tag [tag style-map xs]
  (map (fn [x] [tag style-map x]) xs))