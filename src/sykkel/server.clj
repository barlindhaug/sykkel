(ns sykkel.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [sykkel.core :as core]))

(defn handle-stava-token [code error]
  (if (some? code) (core/fetch-oauth-token code))
  (str (if (= error "access_denied")
         "<p>Du har ikke gitt oss tilgang</p>"
         "<p>Alt ok</p>")
    "<a href=\"/\">Tilbake</a>"))

(defroutes app
           (GET "/" []
              (str
                "<h1>Iterate sykkel challenge 2015</h1>"
                "<h2>\"Sykle til jobben\" (21. april - 19. juni)</h2>"
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
                  (core/go))
                "</ol>"
                "<br />"
                "Koblet til Strava: <span style=\"color:green;\">●</span> "
                "<br />"
                "<h2>Koble til Strava</h2>"
                "<p>APIet til Strava gir tilgang til de 200 siste sykkelturene i hver klubb.</p>"
                "<p>For å få tilgang til alle sykkelturene dine må du gi oss lesetilgang til de offentilige dataene dine på strava.</p>"
                "<a href=\"https://www.strava.com/oauth/authorize?client_id=5814&response_type=code&redirect_uri=https://sykkel.app.iterate.no/connected\">"
                  "<img src=\"http://strava.github.io/api/images/ConnectWithStrava.png\">"
                "</a>"
                ))
           (GET "/connected" [code error]
             (handle-stava-token code error))
           (route/not-found "<h1>Page not found</h1>"))

(defonce ^{:static true} server (atom nil))

(defn -main [port] ;; entry point
  (let [port (Integer/parseInt port)]
    (reset! server (run-jetty app {:port port, :join? false}))
    (str "Started at port " port)))

(comment ;; for use in REPL
  (-main "5000")
  (.stop @server)
  )
