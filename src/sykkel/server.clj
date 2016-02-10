(ns sykkel.server
  (:require [clj-time.core :as time]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer :all]
            [sykkel.core :as core]))

(defn handle-strava-token [code error]
  (if (some? code) (core/fetch-oauth-token code))
  (str (if (= error "access_denied")
         "<p>Du har ikke gitt oss tilgang</p>"
         "<p>Alt ok</p>")
    "<a href=\"/\">Tilbake</a>"))

(defroutes app
           (GET "/" []
               (core/update-recent-club-activities)
               (str
                 (let [start-date (time/date-time 2015 12 01)
                       end-date (time/date-time 2016 02 29)
                       activity-type "Ride"
                       data (core/go start-date end-date activity-type)]
                   (str
                    "<h1>Iterate vintersykkel challenge 2015/16</h1>"
                    "<h2>\"Desember 2015 - Februar 2016\"</h2>"
                    "<h3> Totalt: <strong>"(core/get-total data) "km</strong></h3>"
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
                   "</ol>"
                   "<br />"))
                  (let [start-date (time/date-time 2016 01 01)
                        end-date (time/date-time 2017 01 01)
                        activity-type "Ride"
                        data (core/go start-date end-date activity-type)]
                    (str
                     "<h1>Iterate sykkel challenge 2016</h1>"
                     "<h2>\"Hele 2016\"</h2>"
                     "<h3> Totalt: <strong>"(core/get-total data) "km</strong></h3>"
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
                     "</ol>"
                     "<br />"))
                  (let [start-date (time/date-time 2015 01 01)
                        end-date (time/date-time 2016 01 01)
                        activity-type "Ride"
                        data (core/go start-date end-date activity-type)]
                   (str
                    "<h1>Iterate sykkel challenge 2015</h1>"
                    "<h2>\"Hele 2015\"</h2>"
                    "<h3> Totalt: <strong>"(core/get-total data) "km</strong></h3>"
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
                   "</ol>"
                   "<br />"))
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
                          (core/update-all-activities))
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
