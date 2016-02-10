(ns sykkel.update-activities
  (:require
    [sykkel.db :as db]
    [sykkel.strava :as strava]))

(defn extract-athlete-name [activity]
  (let [athlete-info (:athlete activity)]
    (assoc activity
      :athlete-id (:id athlete-info)
      :athlete-name (str (:firstname athlete-info) " " (:lastname athlete-info)))))

(defn update-activity-in-db [activity]
  (db/insert-activity
    (assoc
      (select-keys activity [:name :type :distance :moving_time :elapsed_time :total_elevation_gain :average_speed :max_speed])
      :strava_id (:id activity)
      :athlete_id (:id (:athlete activity))
      :start_date (db/to-timestamp (:start_date activity)))))

(defn insert-athlete [athlete]
  (db/insert-user
   {:strava_id (:athlete-id athlete)
    :name (:athlete-name athlete)}))

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


(defn get-all-activities-for-auth-users [{:keys [token]}]
  (let [activities (strava/get-athlete-activities token)]
    (dorun (map update-activity-in-db activities))
    (count activities)))

(defn update-all-activities []
  (->> (db/users)
       (filter #(:token %))
       (map get-all-activities-for-auth-users)))
