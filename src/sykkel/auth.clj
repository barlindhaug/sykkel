(ns sykkel.auth
  (:require
   [clj-http.client :as client]
   [clojure.data.json :as json]
   [sykkel.db :as db]))

(def oauth-url "https://www.strava.com/oauth/token")
(def client-id "5814")

(defn oauth-token-from-code [code]
  (let [result (client/post
                oauth-url
                {:form-params {:client_id client-id
                               :client_secret (System/getenv "STRAVA_CLIENT_SECRET")
                               :code code}})
        account-info (json/read-str (:body result) :key-fn keyword)
        athlete-info (:athlete account-info)
        {:keys [firstname lastname id]} athlete-info]
    {:strava_id id
     :name (str firstname " " lastname)
     :token (:access_token account-info)}))

(defn fetch-oauth-token [code]
  (db/insert-user (oauth-token-from-code code)))
