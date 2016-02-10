(ns sykkel.core
  (:require [clj-time.core :as time]
            [clj-time.format :as time-format]
            [sykkel.db :as db]
            [sykkel.strava :as strava]))

(def start-date (time/date-time 2016 02 01))
(def end-date (time/date-time 2016 02 29))

(defn rides-filter [activity]
  (= (:type activity) "Ride"))

(defn period-filter [start end]
  (fn [activity]
    (let [activity-start (time-format/parse (:start_date_local activity))]
      (and
        (time/after? activity-start start)
        (time/before? activity-start end)))))

(defn extract-athlete-name [athlete-activities]
  (let [athlete-info (:athlete athlete-activities)]
    (assoc athlete-activities
      :id (:id athlete-info)
      :name (str (:firstname athlete-info) " " (:lastname athlete-info)))))

(defn add-athlete-name [[id activities]]
  (let [name (:name (first activities))]
    {:id id :activities activities :name name}))

(defn find-activities [{:keys [activities token]}]
  (if token
    (strava/get-athlete-activities token)
    activities))

(defn update-activity-in-db [activity]
  (db/insert-activity
    (assoc
      (select-keys activity [:name :type :distance :moving_time :elapsed_time :total_elevation_gain :average_speed :max_speed])
      :strava_id (:id activity)
      :athlete_id (:id (:athlete activity))
      :start_date (db/to-timestamp (:start_date activity)))))

(defn handle-athletes-activities [athlete]
  (let [activities (find-activities athlete)
        filtered-activities (->>
                              (filter (period-filter start-date end-date) activities)
                              (filter rides-filter))]
    (dorun (map update-activity-in-db activities))
    (assoc athlete
      :activities filtered-activities)))

(defn handle-athlete-stats [athlete]
  (let [stats (strava/get-athlete-stats athlete)]
    (assoc athlete
      :distance (-> (:ytd_ride_totals stats)
                    (:distance)
                    (/ 1000)
                    (int)))))

(defn check-token-for-athlete [athlete]
  (assoc athlete
    :token (db/user-token (:id athlete))))

(defn insert-athlete [athlete]
  (db/insert-user
    (assoc
      (select-keys athlete [:token :name])
      :strava_id (:id athlete)))
  athlete)

(defn sum [activities keyword]
  (reduce + (map #(keyword %) activities)))

(defn sum-distance-per-athlete [athlete]
  (assoc athlete
    :distance (-> (sum (:activities athlete) :distance)
                  (/ 1000)
                  (int))))

(defn sort-by-distance [results]
  (reverse (sort-by :distance results)))

(defn get-total [results]
  (reduce + (map #(:distance %) results)))

(defn go []
  (->> (strava/get-club-activities)
    (map extract-athlete-name)
    (group-by :id)
    (map add-athlete-name)
    (map check-token-for-athlete)
    (map insert-athlete)
    (map handle-athletes-activities)
    (map sum-distance-per-athlete)
    (sort-by-distance)))

(defn year-to-date []
  (->> (db/users)
       (filter #(:token %))
       (map handle-athlete-stats)
       (sort-by-distance)))

(defn fetch-oauth-token [code]
  (db/insert-user (strava/oauth-token-from-code code)))
