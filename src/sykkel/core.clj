(ns sykkel.core
  (:require [sykkel.db :as db]))

(defn sum [results keyword]
  (reduce + (map #(keyword %) results)))
