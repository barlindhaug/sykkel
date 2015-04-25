(ns sykkel.core
  (:require [clj-http.client :as client]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [clj-time.coerce :as time-corece]))

(def activities-url "https://www.strava.com/api/v3/clubs/64726/activities")
(def auth-token (System/getenv "STRAVA_API_KEY"))
(def start-date (time/date-time 2015 04 21))

(defn get-file []
  (:body
    (client/get activities-url
                {:query-params {:per_page 200}
                 :headers {"Authorization" (str "Bearer " auth-token)}
                 :accept :json
                 :as :json})))


(defn filter-rides [activities]
  (filter #(= (:type %) "Ride") activities))

(defn filter-period [start activities]
  (filter
    (fn [activity]
      (time/after?
        (time-format/parse (:start_date_local activity))
        start))
    activities))

(defn extract-athlete-id [activities]
  (map
    (fn [athlete-activities]
      (let [athlete-info (:athlete athlete-activities)]
        (assoc athlete-activities
          :athlete_id (:id athlete-info)
          :name (str (:firstname athlete-info) " " (:lastname athlete-info)))))
    activities))

(defn group-by-athletes [activities]
  (group-by :name activities))

(defn sum [activities keyword]
  (reduce + (map #(keyword %) activities)))

(defn sum-distance-per-athlete [activities]
  (map
    (fn [[name athlete]]
      {:distance (-> (sum athlete :distance)
                     (/ 1000)
                     (int))
       :name name})
    activities))

(defn sort-by-distance [results]
  (reverse (sort-by :distance results)))

(defn go []
  (->> (get-file)
       (filter-rides)
       (filter-period start-date)
       (extract-athlete-id)
       (group-by-athletes)
       (sum-distance-per-athlete)
       (sort-by-distance)))

(defn extract [skiing-activities]
  (map #(select-keys % [:distance :start_date_local]) skiing-activities))
