(ns sykkel.core
  (:require [clj-http.client :as client]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [clojure.data.json :as json]
            [sykkel.db :as db]))

(def base-url "https://www.strava.com/api/v3/")
(def oauth-url "https://www.strava.com/oauth/token")

(def auth-token (System/getenv "STRAVA_API_KEY"))
(def start-date (time/date-time 2015 04 21))

(defn club-activities-url []
  (str base-url "clubs/64726/activities"))

(defn athlete-activities-url []
  (str base-url "athlete/activities"))

(defn get-file
  ([url]
   (get-file url auth-token))
  ([url token]
   (:body
     (client/get url
       {:query-params {:per_page 200}
        :headers {"Authorization" (str "Bearer " token)}
        :accept :json
        :as :json}))))

(defn get-activities []
  (get-file (club-activities-url)))

(defn get-athlete-activities [athlete-token]
  (get-file (athlete-activities-url) athlete-token))

(defn oauth-token-from-code [code]
  (let [result (client/post oauth-url {:form-params {:client_id "5814"
                                                     :client_secret (System/getenv "STRAVA_CLIENT_SECRET")
                                                     :code code}})
        account-info (json/read-str (:body result) :key-fn keyword)
        athlete-info (:athlete account-info)
        {:keys [firstname lastname id]} athlete-info]
    {:strava_id id
     :name (str firstname " " lastname)
     :token (:access_token account-info)}))

(defn filter-rides [activities]
  (filter #(= (:type %) "Ride") activities))

(defn filter-period [start activities]
  (filter
    (fn [activity]
      (time/after?
        (time-format/parse (:start_date_local activity))
        start))
    activities))

(defn extract-athlete-name [activities]
  (map
    (fn [athlete-activities]
      (let [athlete-info (:athlete athlete-activities)]
        (assoc athlete-activities
          :id (:id athlete-info)
          :name (str (:firstname athlete-info) " " (:lastname athlete-info)))))
    activities))

(defn group-by-athletes [activities]
  (map
    (fn [[id activities]]
      (let [name (:name (first activities))]
        {:id id :activities activities :name name}))
    (group-by :id activities)))

(defn find-activities [{:keys [activities token]}]
  (if token
    (get-athlete-activities token)
    activities))

(defn handle-athletes-activities [athletes]
  (map (fn [athlete]
         (let [activities (find-activities athlete)
               filtered-activities (->>
                                     (filter-period start-date activities)
                                     (filter-rides))]
           (assoc athlete
             :activities filtered-activities))
         )
    athletes))

(defn check-token-for-athlete [athletes]
  (map (fn [athlete]
         (assoc athlete
           :token (db/user-token (:id athlete))))
    athletes))

(defn sum [activities keyword]
  (reduce + (map #(keyword %) activities)))

(defn sum-distance-per-athlete [athletes]
  (map
    (fn [athlete]
      (assoc athlete
        :distance (-> (sum (:activities athlete) :distance)
                      (/ 1000)
                      (int))))
    athletes))

(defn sort-by-distance [results]
  (reverse (sort-by :distance results)))

(defn go []
  (->> (get-activities)
    (extract-athlete-name)
    (group-by-athletes)
    (check-token-for-athlete)
    (handle-athletes-activities)
    (sum-distance-per-athlete)
    (sort-by-distance)))

(defn fetch-oauth-token [code]
  (db/insert-user (oauth-token-from-code code)))
