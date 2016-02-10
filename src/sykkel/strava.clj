(ns sykkel.strava
  (:require
    [clj-http.client :as client]))

(def base-url "https://www.strava.com/api/v3/")

(def auth-token (System/getenv "STRAVA_API_KEY"))
(def max-page-size 200)

(def club-activities-url
  (str base-url "clubs/64726/activities"))

(def athlete-activities-url
  (str base-url "athlete/activities"))

(defn call-api
  ([url]
   (call-api url auth-token))
  ([url token]
   (:body
     (client/get url
       {:query-params {:per_page max-page-size}
        :headers {"Authorization" (str "Bearer " token)}
        :accept :json
        :as :json})))
  ([url token page]
   (:body
     (client/get url
       {:query-params {:per_page max-page-size
                       :page page}
        :headers {"Authorization" (str "Bearer " token)}
        :accept :json
        :as :json}))))

(defn get-club-activities []
  (call-api club-activities-url))

(defn get-athlete-activities-with-pagination [athlete-token activities page]
  (let [new-activities (call-api athlete-activities-url athlete-token page)
        all-activities (concat activities new-activities)]
    (if (= (count new-activities) max-page-size)
      (recur athlete-token all-activities (+ 1 page))
      all-activities)))

(defn get-athlete-activities [athlete-token]
  (let [start-page 1]
    (get-athlete-activities-with-pagination athlete-token [] start-page)))
