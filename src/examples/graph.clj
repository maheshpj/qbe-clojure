(ns examples.graph)


(defrecord Node [foo bar baz])

(defn node [foo bar baz] (Node. foo bar baz))

(def the-graph {})

(defn add-node [g n]
  (if (g n)
    g
    (assoc g n {:next #{} :prev #{}})))

(defn add-edge [g n1 n2]
  (-> g
      (add-node n1)
      (add-node n2)
      (update-in [n1 :next] conj n2)
      (update-in [n2 :prev] conj n1)))

(defn remove-edge [g n1 n2]
  (-> g
      (add-node n1)
      (add-node n2)
      (update-in [n1 :next] disj n2)
      (update-in [n2 :prev] disj n1)))

(defn remove-node [g n]
  (if-let [{:keys [next prev]} (g n)]
    ((comp
      #(dissoc % n)
      #(reduce (fn [g* n*] (remove-edge g* n* n)) % prev)
      #(reduce (fn [g* n*] (remove-edge g* n n*)) % next))
     g)
    g))

(defn contains-node? [g n]
  (g n))

(defn contains-edge? [g n1 n2]
  (get-in g [n1 :next n2]))

(defn next-nodes [g n]
  (get-in g [n :next]))

;; Assumes DAG
(defn depth-first-search [g root-node goal?]
  (loop [open-list (list root-node)]
    (when-first [n open-list]
      (if (goal? n)
        n
        (recur (concat (next-nodes g n) (rest open-list))))))) 