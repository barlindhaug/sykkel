(ns sykkel.core
  (:require [clj-time.coerce :as time-coerce]
            [clj-time.core :as time]
            [sykkel.db :as db]))

(defn sum [results keyword]
  (reduce + (map #(keyword %) results)))

(defn split-at-now [challenges]
  (split-with
    #(time/before? (time/now) (:end_date %))
    challenges))

(defn oldest-first [challenges]
  (sort-by :end_date time/before? challenges))

(defn newest-first [challenges]
  (sort-by :end_date time/after? challenges))

(defn sort-challenges [challenges]
  (let [[after-now before-now] (split-at-now challenges)]
    (concat (oldest-first after-now)
            (newest-first before-now))))

(defn convert-dates [challenge]
  (assoc challenge :end_date (time-coerce/from-sql-date (:end_date challenge))))

(defn challenges [type]
  (->>
    (db/challenges type)
    (map convert-dates)
    (sort-challenges)))

(defn format-number [number]
  (if (> number 1000)
    (str (int (/ number 1000)) "." (format "%03d" (int (mod number 1000))))
    (str number)))