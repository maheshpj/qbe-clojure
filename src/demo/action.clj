(ns demo.action
  (:require [demo.db :as db]
            [clojure.string :only (capitalize replace-first upper-case) :as st]))

(def op)
(def cr)
(def rt)
(def ord)

(defn
  filter-req
  [prefix req-map]
  (into {}
        (filter #(.startsWith (name (key %)) prefix) 
                req-map)))

(defn
  remove-db-prefix
  [kee prefix]
  (st/replace-first 
       (name kee) (str prefix ".") ""))
(defn
  filter-list-by-prefix
  "Return list of filtered request with prefix"
  [prefix req-map]
  (let [mp (filter-req prefix req-map)]
    (map #(remove-db-prefix % prefix)
         (keys mp))))

(defn
  filter-map-by-prefix
  "Return map of filtered request with prefix"
  [prefix req-map]
  (let [crmap (filter-req prefix req-map)]
    (println "crmap is " crmap)
    (zipmap 
      (map #(keyword 
              (remove-db-prefix % prefix))
           (keys crmap))
      (vals crmap))))

(defn
  create-query-seqs
  [req-map]
  (def op (filter-list-by-prefix "CLM" req-map))
  (def cr (filter-map-by-prefix "TXT" req-map))
  (def ord (filter-list-by-prefix "ORD" req-map))
  (def rt (:RT req-map)))

(defn
  get-result
  []
  (when-not (utils/if-nil-or-empty op)
    (if (db/is-db-type-ora)
      (db/execute-query 
        (db/create-query-str-for-ora op cr rt ord))
      (db/execute-query 
        (db/create-query-str-for-pg op cr rt ord)))))

(defn
  get-schema
  []
  (db/fetch-db-table-columns-map))

(defn
  create-headers
  [prefix replaceby coll]
  (map #(st/capitalize 
          (st/replace-first 
            (st/upper-case %) prefix replaceby)) 
       coll))

(defn
  get-header-clms
  []
  (if (db/is-db-type-ora)
    (create-headers "AMS_" "" op)
    (create-headers "RP_" "" op)))