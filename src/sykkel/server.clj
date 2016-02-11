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

(defn header [current-page]
  (str
   "<div class=\"nav-container\">"
     "<h1 class=\"title\">SYKKL</h1>"
     "<div class=\"nav\">"
       "<a "
         (when
           (or
            (= current-page "Ride")
            (= current-page ""))
            "class=\"current\"")
         "href=\"/ride\">Sykkel</a> | "
       "<a "
         (when (= current-page "NordicSki")
            "class=\"current\"")
         "href=\"/ski\">Ski</a> | "
       "<a "
         (when (= current-page "Run")
            "class=\"current\"")
         "href=\"/run\">L&oslash;ping</a>"
     "</div>"
   "</div>"))

(def footer
  (str
    "<p>Koblet til Strava: <span style=\"color:green;\">●</span></p>"
    "<h2>Koble til Strava</h2>"
    "<p>APIet til Strava gir tilgang til de 200 siste sykkelturene i hver klubb.</p>"
    "<p>For å få tilgang til alle sykkelturene dine må du gi oss lesetilgang til de offentilige dataene dine på strava.</p>"
    "<a href=\"https://www.strava.com/oauth/authorize?client_id=5814&response_type=code&redirect_uri=https://sykkel.app.iterate.no/connected\">"
    "<img src=\"http://strava.github.io/api/images/ConnectWithStrava.png\">"
    "</a>"
    "<br />"
    "<a href=\"https://github.com/barlindhaug/sykkel\">GitHub</a>"))

(defn challenge-totals-html [challenge]
  (let [data (db/challenge-totals (:id challenge))]
    (str
      "<table>"
      "<tr><th>Navn</th><th>Total avstand</th><th>Lengste tur</th><th>Klatret</th></tr>"
      (reduce
        (fn [list result]
          (let [color (if (:token result) "green" "red")]
            (str
              list
              "<tr>"
              "<td>"
              (:name result)
              " <span style=\"color: " color ";\">●</span>"
              "</td>"
              "<td><strong>" (:distance result) " km</strong></td>"
              "<td><strong>" (:longest result) " km</strong></td>"
              "<td><strong>" (:climbed result) " m</strong></td>"
              "</tr>")))
        ""
        data)
      "</table>"
      "<strong>Totalt: "(core/sum data :distance) " km</strong>")))

(defn challenge-top-html [challenge]
  (let [field (keyword (:field challenge))
        data (db/challenge-top-results (:id challenge))]
    (str
      "<table>"
      "<tr><th>Navn</th><th>" field "</th><th>Tid</th><th>Dato</th></tr>"
      (reduce
        (fn [list result]
          (let [color (if (:token result) "green" "red")
                date (time-coerce/from-sql-date (:start_date result))
                moving-time (time/plus (time/today-at 0 0) (time/seconds (:moving_time result)))]
            (str
              list
              "<tr>"
              "<td>"
              (:name result)
              " <span style=\"color: " color ";\">●</span>"
              "</td>"
              "<td><strong>" (int (/ (field result) 1000)) " km</strong></td>"
              "<td><strong>" (time-format/unparse time-formatter moving-time) "</strong></td>"
              "<td>" (time-format/unparse date-formatter date) "</td>"
              "</tr>")))
        ""
        data)
      "</table>")))

(defn challenge-html [challenge]
  (let [name (:name challenge)
        description (:description challenge)]
    (str
      "<h2>" name "</h2>"
      (when description
        (str "<h3>" description "</h3>"))
      (case (:type challenge)
        "totals" (challenge-totals-html challenge)
        "top" (challenge-top-html challenge)))))

(defn challenges-html [type]
  (let [challenges (db/challenges type)]
    (update-activities/update-recent-club-activities)
    (str
      "<html>"
      "<head>"
      "  <link href=\"/style.css\" rel=\"stylesheet\">"
      "</head>"
      "<body>"
      (header type)
      (apply str (map challenge-html (db/challenges type)))
      footer
      "</body>"
      "</html>")))

(defroutes app
           (GET "/" []
             (challenges-html "Ride"))
           (GET "/ride" []
             (challenges-html "Ride"))
           (GET "/run" []
             (challenges-html "Run"))
           (GET "/ski" []
             (challenges-html "NordicSki"))
           (GET "/connected" [code error]
             (handle-strava-token code error))
           (GET "/update-all-activities" []
             (str "added "
                  (reduce (fn [string count] (str string count " "))
                          ""
                          (update-activities/update-all-activities))
                  "activities"))
           (route/resources "/")
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
