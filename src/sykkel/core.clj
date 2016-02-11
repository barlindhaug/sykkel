(ns sykkel.core)

(defn sum [results keyword]
  (reduce + (map #(keyword %) results)))
