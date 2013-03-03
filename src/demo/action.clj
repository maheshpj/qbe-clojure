(ns demo.action
  (:use [demo.db]))

(defn
  content
  []
  (fetch-db-table-columns-map))

