(ns sykkel.server
  (:require [clj-time.coerce :as time-coerce]
            [clj-time.core :as time]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer :all]
            [sykkel.auth :as auth]
            [sykkel.core :as core]
            [sykkel.db :as db]
            [sykkel.update-activities :as update-activities]))

(defn handle-strava-token [code error]
  (if (some? code) (auth/fetch-oauth-token code))
  (str (if (= error "access_denied")
         "<p>Du har ikke gitt oss tilgang</p>"
         "<p>Alt ok</p>")
    "<a href=\"/\">Tilbake</a>"))

(defn challenge-html [challenge]
  (let [name (:name challenge)
        description (:description challenge)
        data (core/challenge-results (:id challenge))]
    (str
      "<h1>" name "</h1>"
      (when description
        (str "<h2>" description "</h2>"))
      "<h3> Totalt: <strong>"(core/sum data :distance) "km</strong></h3>"
      "<ol style=\"list-style-type: decimal;\">"
      (reduce
        (fn [list result]
          (let [color (if (:token result) "green" "red")]
            (str
              list
              "<li>"
              (:name result) " <strong>" (:distance result) "km</strong>"
              " <span style=\"color: " color ";\">●</span>"
              "</li>")))
        ""
        data)
      "</ol>")))

(defroutes app
           (GET "/" []
               (update-activities/update-recent-club-activities)
               (str
                 (apply str (map challenge-html (db/challenges)))
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
                   "<a href=\"https://github.com/barlindhaug/sykkel\">GitHub</a>"
                 )))
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
