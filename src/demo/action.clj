(ns demo.action
  (:use [utils])
  (:require [demo.db :as db]
            [clojure.string :only (capitalize replace-first upper-case) :as st]))

(def op)
(def cr)
(def rt)
(def ord)
(def grp)
(def mf)
(def ch-op)
(def ch-grp)
(def ch-ord)
(def ch-cr)
(def mem-mata-fields nil)
(def err "Invalid Criteria")
(def err_grp "Please select only one Group Function column.")

(defn
  filter-req
  [prefix req-map]
  (into {} (filter #(.startsWith (name (key %)) prefix) req-map)))

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
    (map #(remove-db-prefix % prefix) (keys mp))))

(defn
  filter-map-by-prefix
  "Return map of filtered request with prefix"
  [prefix req-map]
  (let [crmap (filter-req prefix req-map)]
    (zipmap (map #(keyword (remove-db-prefix % prefix))
                 (keys crmap))
            (vals crmap))))

(defn
  mf-to-clm
  [mf clmval]
  (let [kee (keyword clmval)]
    (if (contains? mf kee) (get mf kee) clmval)))

(defn
  rpl-clms
  [mf clms]
  (when-not (and (if-nil-or-empty mf) (if-nil-or-empty clms))
    (map #(mf-to-clm mf %) clms)))

(defn
  change-params
  [mf op grp ord cr]
  (def ch-op (rpl-clms mf op))
  (def ch-ord (rpl-clms mf ord))
  (def ch-grp (zipmap (rpl-clms mf (keys grp)) (vals grp)))
  (def ch-cr (zipmap (rpl-clms mf (keys cr)) (vals cr))))

(defn
  filter-RT
  [req-map]
  (first (filter #(= (st/upper-case (str prf ((keyword RT) req-map))) 
                     (st/upper-case %)) (keys db/cached-schema))))

(defn
  get-mf
  [pmf]
  (zipmap (map keyword pmf) (map #(str meta_alias (.indexOf pmf %) "." (:COLUMN metadata-value)) pmf)))

(defn
  create-query-seqs
  [req-map]
  (def op (filter-list-by-prefix CLM req-map))
  (def ch-op op)
  (def cr (filter-map-by-prefix TXT req-map))
  (def ch-cr cr)
  (def ord (filter-list-by-prefix ORD req-map))
  (def ch-ord ord)
  (def grp (filter-map-by-prefix GRP req-map))
  (def ch-grp grp)
  (def rt (filter-RT req-map))
  (let [pmf (filter-list-by-prefix MTA req-map)]
    (def mf (get-mf pmf)))
  (when-not (if-nil-or-empty mf)
    (change-params mf op grp ord cr)))

(defn
  get-result
  []
  (when-not (utils/if-nil-or-empty op)
    (if (> (count grp) 1)
      {:Error err_grp}
      (try
        (db/execute-query 
          (db/create-query-str op ch-op cr ch-cr rt ord ch-ord 
                               (first grp) (first ch-grp) mf))
        (catch Exception _ {:Error err})))))

(defn
  get-schema
  []
  (db/set-util-prf)
  (db/fetch-db-table-columns-map)
  (db/get-pk-ralation db/cached-schema)
  (db/create-db-graph)
  db/cached-schema)

(defn
  create-headers
  [prefix replaceby coll]
  (map #(st/capitalize 
          (st/replace-first 
            (st/upper-case %) prefix replaceby)) coll))

(defn
  get-header-clms
  []
  (create-headers (val-up prf) "" op))     