(ns demo.action
  (:use [demo.db]))

(defn
  content
  []
  (execute-query test-query))


(defn
  content2
  []
  (fetch-table-columns-map))
