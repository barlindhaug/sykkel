(ns sykkel.core
  (:require [sykkel.db :as db]
            [sykkel.strava :as strava]))

(defn extract-athlete-name [activity]
  (let [athlete-info (:athlete activity)]
    (assoc activity
      :athlete-id (:id athlete-info)
      :athlete-name (str (:firstname athlete-info) " " (:lastname athlete-info)))))

(defn add-user-data [[athlete-id activities]]
  (let [user (db/user athlete-id)
        name (:name user)
        token (:token user)]
    {:id athlete-id :activities activities :name name :token token}))

(defn insert-athlete [athlete]
  (db/insert-user
    {:strava_id (:athlete-id athlete)
     :name (:athlete-name athlete)
    }))

(defn update-activity-in-db [activity]
  (db/insert-activity
    (assoc
      (select-keys activity [:name :type :distance :moving_time :elapsed_time :total_elevation_gain :average_speed :max_speed])
      :strava_id (:id activity)
      :athlete_id (:id (:athlete activity))
      :start_date (db/to-timestamp (:start_date activity)))))

(defn update-activities-for-athlete [[athlete-id activities]]
  (when (not (db/user athlete-id))
    (insert-athlete (first activities)))
  (dorun (map update-activity-in-db activities)))

(defn update-athlete-activities [athletes-activities]
  (dorun (map update-activities-for-athlete athletes-activities)))

(defn update-recent-club-activities []
  (->> (strava/get-club-activities)
    (map extract-athlete-name)
    (group-by :athlete-id)
    (update-athlete-activities)))


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

(defn get-all-activities-for-auth-users [{:keys [token]}]
  (let [activities (strava/get-athlete-activities token)]
    (dorun (map update-activity-in-db activities))
    (count activities)))

(defn update-all-activities []
  (->> (db/users)
       (filter #(:token %))
       (map get-all-activities-for-auth-users)))
