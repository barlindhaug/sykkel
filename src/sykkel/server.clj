(ns sykkel.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [sykkel.core :as core]))

(defroutes app
           (GET "/" []
              (str
                "<h1>Iterate sykkel challenge 2015</h1>"
                "<ul>"
                (reduce
                  (fn [list result]
                    (str list "<li>" (:name result) " " (:distance result) "</li>"))
                  ""
                  (core/go 2015))
                  "</ul>"))
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
