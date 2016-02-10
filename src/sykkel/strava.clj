(ns sykkel.strava
  (:require
    [clj-http.client :as client]
    [clojure.data.json :as json]))

(def base-url "https://www.strava.com/api/v3/")
(def oauth-url "https://www.strava.com/oauth/token")

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

(defn get-athlete-activities-with-pagination [athlete-token activities page]
  (let [new-activities (call-api athlete-activities-url athlete-token page)
        all-activities (concat activities new-activities)]
    (if (= (count new-activities) max-page-size)
      (recur athlete-token all-activities (+ 1 page))
      all-activities)))

(defn get-athlete-activities [athlete-token]
  (let [start-page 1]
    (get-athlete-activities-with-pagination athlete-token [] start-page)))
