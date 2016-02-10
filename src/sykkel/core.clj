(ns sykkel.core
  (:require [sykkel.db :as db]
            [sykkel.strava :as strava]))

(defn add-user-data [[athlete-id activities]]
  (let [user (db/user athlete-id)
        name (:name user)
        token (:token user)]
    {:id athlete-id :activities activities :name name :token token}))

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

(defn go [start-date end-date activity-type]
  (->> (db/activities start-date end-date activity-type)
    (group-by :athlete_id)
    (map add-user-data)
    (map sum-distance-per-athlete)
    (sort-by-distance)))

(defn fetch-oauth-token [code]
  (db/insert-user (strava/oauth-token-from-code code)))
