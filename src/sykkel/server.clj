(ns sykkel.server
  (:require [clj-time.coerce :as time-coerce]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer :all]
            [sykkel.auth :as auth]
            [sykkel.core :as core]
            [sykkel.db :as db]
            [sykkel.update-activities :as update-activities]))

(def date-formatter (time-format/formatter "d/M yyyy"))
(def time-formatter (time-format/formatter "HH:mm:ss"))

(defn handle-strava-token [code error]
  (if (some? code) (auth/fetch-oauth-token code))
  (str (if (= error "access_denied")
         "<p>Du har ikke gitt oss tilgang</p>"
         "<p>Alt ok</p>")
    "<a href=\"/\">Tilbake</a>"))

(def footer
  (str
    "Koblet til Strava: <span style=\"color:green;\">●</span> "
    "<br />"
    "<h2>Koble til Strava</h2>"
    "<p>APIet til Strava gir tilgang til de 200 siste sykkelturene i hver klubb.</p>"
    "<p>For å få tilgang til alle sykkelturene dine må du gi oss lesetilgang til de offentilige dataene dine på strava.</p>"
    "<a href=\"https://www.strava.com/oauth/authorize?client_id=5814&response_type=code&redirect_uri=https://sykkel.app.iterate.no/connected\">"
    "<img src=\"http://strava.github.io/api/images/ConnectWithStrava.png\">"
    "</a>"
    "<br />"
    "<a href=\"https://github.com/barlindhaug/sykkel\">GitHub</a>"))

(defn challenge-totals-html [challenge]
  (let [name (:name challenge)
        description (:description challenge)
        data (db/challenge-totals (:id challenge))]
    (str
      "<h1>" name "</h1>"
      (when description
        (str "<h2>" description "</h2>"))
      "<h3> Totalt: <strong>"(core/sum data :distance) " km</strong></h3>"
      "<ol style=\"list-style-type: decimal;\">"
      (reduce
        (fn [list result]
          (let [color (if (:token result) "green" "red")]
            (str
              list
              "<li>"
              (:name result)
              " <strong>" (:distance result) " km</strong> -"
              " longest ride: <strong>" (:longest result) " km</strong> -"
              " climbed <strong>" (:climbed result) " m</strong>"
              " <span style=\"color: " color ";\">●</span>"
              "</li>")))
        ""
        data)
      "</ol>")))

(defn challenge-top-html [challenge]
  (let [name (:name challenge)
        description (:description challenge)
        field (keyword (:field challenge))
        data (db/challenge-top-results (:id challenge))]
    (str
      "<h1>" name "</h1>"
      (when description
        (str "<h2>" description "</h2>"))
      "<ol style=\"list-style-type: decimal;\">"
      (reduce
        (fn [list result]
          (let [color (if (:token result) "green" "red")
                date (time-coerce/from-sql-date (:start_date result))
                moving-time (time/plus (time/today-at 0 0) (time/seconds (:moving_time result)))]
            (str
              list
              "<li>"
              (:name result) " <strong>" (int (/ (field result) 1000)) " km</strong> - "
              (time-format/unparse time-formatter moving-time) " - "
              (time-format/unparse date-formatter date)
              " <span style=\"color: " color ";\">●</span>"
              "</li>")))
        ""
        data)
      "</ol>")))

(defn challenge-html [challenge]
  (case (:type challenge)
    "totals" (challenge-totals-html challenge)
    "top" (challenge-top-html challenge)))

(defroutes app
           (GET "/" []
               (update-activities/update-recent-club-activities)
               (str
                 (apply str (map challenge-html (db/challenges)))
                 footer))
           (GET "/connected" [code error]
             (handle-strava-token code error))
           (GET "/update-all-activities" []
             (str "added "
                  (reduce (fn [string count] (str string count " "))
                          ""
                          (update-activities/update-all-activities))
                  "activities"))
           (route/not-found "<h1>Page not found</h1>"))

(defonce ^{:static true} server (atom nil))

(defn -main [port] ;; entry point
  (let [port (Integer/parseInt port)]
    (reset! server (run-jetty (wrap-defaults app site-defaults) {:port port, :join? false}))
    (str "Started at port " port)))

(comment ;; for use in REPL
  (-main "5000")
  (.stop @server)
  )
